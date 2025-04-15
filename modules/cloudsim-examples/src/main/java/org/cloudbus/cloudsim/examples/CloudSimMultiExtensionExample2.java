package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;
import org.cloudbus.cloudsim.examples.network.datacenter.NetworkConstants;
import org.cloudbus.cloudsim.network.datacenter.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class CloudSimMultiExtensionExample2 {

	private static List<GuestEntity> guestList;
	private static List<GuestEntity> containerList = new ArrayList<>();

	private static List<HostEntity> hostList = new ArrayList<>();

	private static NetworkDatacenter datacenter;

	private static DatacenterBroker broker;

	private static final int numberOfHosts = 2;
	private static final int numberOfVms = 4;

	/**
	 * Example of tandem DAG: A ---> B
	 * with Datacenter configuration:        switch
	 * 								   		 /	 \
	 * 									 Host0    Host1
	 * 						   			VM0 VM2  VM1 VM3
	 *                                      C4
	 *
	 * Depending on the cloudlet placement, the network may be used or not.
	 * 
	 * @param args the args
	 * @author Remo Andreoli
	 */
	public static void main(String[] args) {

		Log.println("Starting TandemAppExample1...");

		try {
			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			datacenter = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			broker = new DatacenterBroker("Broker");

			guestList = CreateVMs(datacenter.getId());
			if (!guestList.isEmpty() && guestList.size() > 1) {
				hostList.add((HostEntity) guestList.get(1));

				GuestEntity container = new NetworkContainer(4, broker.getId(), 100, 1, 1, 1, 1, "Docker",
						new CloudletSchedulerTimeShared());
				container.setVirtualizationOverhead(10);
				container.setHost((HostEntity) guestList.get(1));
				containerList.add(container);
			}

			AppCloudlet app = new AppCloudlet(AppCloudlet.APP_Workflow, 0, 2000, broker.getId());
			createTaskList(app);

			// submit vm list to the broker

			broker.submitGuestList(guestList);
			broker.submitGuestList(containerList);
			broker.submitCloudletList(app.cList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);
			System.out.println("numberofcloudlet " + newList.size() + " Data transfered "
					+ datacenter.totalDataTransfer);

			Log.println("TandemAppExample1 finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.println("Unwanted errors happen");
		}
	}

	/**
	 * Creates the datacenter.
	 * 
	 * @param name
	 *            the name
	 * 
	 * @return the datacenter
	 */
	private static NetworkDatacenter createDatacenter(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("Datacenter name cannot be null or empty");
		}

		// Resource configuration
		int mips = 100000;
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 100000;

		// Validate resource parameters
		if (mips <= 0 || ram <= 0 || storage <= 0 || bw <= 0) {
			throw new IllegalArgumentException("Resource parameters must be positive values");
		}

		List<HostEntity> hostList = new ArrayList<>();
		try {
			for (int i = 0; i < numberOfHosts; i++) {
				List<Pe> peList = new ArrayList<>();
				// Create processing elements
				peList.add(new Pe(0, new PeProvisionerSimple(mips)));
				peList.add(new Pe(1, new PeProvisionerSimple(mips)));

				NetworkHost host = new NetworkHost(
						i,
						new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw),
						storage,
						peList,
						new VmSchedulerTimeShared(peList));

				if (host != null) {
					hostList.add(host);
				}
			}
		} catch (Exception e) {
			Log.println("Error creating hosts: " + e.getMessage());
			throw new RuntimeException("Failed to create datacenter hosts", e);
		}

		if (hostList.isEmpty()) {
			throw new RuntimeException("No hosts could be created for the datacenter");
		}

		// Datacenter characteristics
		String arch = "x86";
		String os = "Linux";
		String vmm = "Xen";
		double time_zone = 10.0;
		double cost = 3.0;
		double costPerMem = 0.05;
		double costPerStorage = 0.001;
		double costPerBw = 0.0;

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch,
				os,
				vmm,
				hostList,
				time_zone,
				cost,
				costPerMem,
				costPerStorage,
				costPerBw);

		NetworkDatacenter datacenter = null;
		try {
			datacenter = new NetworkDatacenter(
					name,
					characteristics,
					new VmAllocationPolicySimple(hostList),
					new LinkedList<Storage>(),
					0);

			// Create Internal Datacenter network
			CreateNetwork(datacenter);

		} catch (Exception e) {
			Log.println("Error creating datacenter: " + e.getMessage());
			throw new RuntimeException("Failed to create datacenter: " + e.getMessage(), e);
		}

		if (datacenter == null) {
			throw new RuntimeException("Failed to create datacenter: datacenter is null");
		}

		return datacenter;
	}

	/**
	 * Prints the Cloudlet objects.
	 * 
	 * @param list
	 *            list of Cloudlets
	 * @throws IOException
	 */
	private static void printCloudletList(List<Cloudlet> list) throws IOException {
		if (list == null) {
			throw new IllegalArgumentException("Cloudlet list cannot be null");
		}

		if (list.isEmpty()) {
			Log.println("No cloudlets to display.");
			return;
		}

		String indent = "    ";
		DecimalFormat dft = new DecimalFormat("###.##");
		
		// Print header
		StringBuilder header = new StringBuilder();
		header.append("\n========== OUTPUT ==========\n");
		header.append(String.format("%-12s%-10s%-16s%-8s%-12s%-14s%-14s",
			"Cloudlet ID",
			"STATUS",
			"Data center ID",
			"VM ID",
			"Time",
			"Start Time",
			"Finish Time"));
		Log.println(header.toString());

		// Print cloudlet details
		try {
			for (Cloudlet cloudlet : list) {
				if (cloudlet == null) {
					Log.println(indent + "WARNING: Null cloudlet encountered, skipping...");
					continue;
				}

				StringBuilder sb = new StringBuilder();
				sb.append(String.format("%-12d", cloudlet.getCloudletId()));

				if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
					sb.append(String.format("%-10s", "SUCCESS"));
					sb.append(String.format("%-16d", cloudlet.getResourceId()));
					sb.append(String.format("%-8d", cloudlet.getGuestId()));
					sb.append(String.format("%-12s", dft.format(cloudlet.getActualCPUTime())));
					sb.append(String.format("%-14s", dft.format(cloudlet.getExecStartTime())));
					sb.append(String.format("%-14s", dft.format(cloudlet.getExecFinishTime())));
				} else {
					sb.append(String.format("%-10s", cloudlet.getStatus()));
					sb.append(" (Failed to complete)");
				}

				Log.println(indent + sb.toString());
			}
		} catch (Exception e) {
			String errorMsg = "Error while printing cloudlet details: " + e.getMessage();
			Log.println(errorMsg);
			throw new IOException(errorMsg, e);
		}

		// Print footer with summary
		int successCount = (int) list.stream()
			.filter(c -> c != null && c.getStatus() == Cloudlet.CloudletStatus.SUCCESS)
			.count();
		
		StringBuilder footer = new StringBuilder();
		footer.append("\nSummary:");
		footer.append("\nTotal Cloudlets: ").append(list.size());
		footer.append("\nSuccessful: ").append(successCount);
		footer.append("\nFailed: ").append(list.size() - successCount);
		footer.append("\n==========================\n");
		Log.println(footer.toString());
	}

	/**
	 * Creates virtual machines in a datacenter
	 * @param datacenterId The id of the datacenter where to create the VMs.
	 */
	private static ArrayList<GuestEntity> CreateVMs(int datacenterId) {
		ArrayList<GuestEntity> vmList = new ArrayList<>();

		try {
			int mips = 1000;
			long size = 10000; // image size (MB)
			int ram = 512; // vm memory (MB)
			long bw = 1000;
			int pesNumber = 1;
			String vmm = "Xen";

			for (int i=0; i<numberOfVms; i++) {
				List<Pe> peList = new ArrayList<>();
				peList.add(new Pe(0, new PeProvisionerSimple(mips/2)));

				NetworkVm vm = new NetworkVm(
						i,
						broker.getId(),
						mips,
						pesNumber,
						ram,
						bw,
						size,
						vmm,
						new CloudletSchedulerTimeShared(),
						new VmSchedulerTimeShared(peList),
						new RamProvisionerSimple(ram),
						new BwProvisionerSimple(bw),
						peList);
				
				vmList.add(vm);
				// Set virtualization overhead for the newly added VM
				vm.setVirtualizationOverhead(20);
			}
		} catch (Exception e) {
			Log.println("Error creating VMs: " + e.getMessage());
		}

		return vmList;
	}

	static private void createTaskList(AppCloudlet appCloudlet) {
		if (appCloudlet == null) {
			throw new IllegalArgumentException("AppCloudlet cannot be null");
		}

		if (appCloudlet.cList == null) {
			appCloudlet.cList = new ArrayList<>();
		}

		// Validate broker and container availability
		if (broker == null) {
			throw new IllegalStateException("Broker is not initialized");
		}

		if (containerList.isEmpty()) {
			throw new IllegalStateException("No containers available for task allocation");
		}

		if (guestList.isEmpty()) {
			throw new IllegalStateException("No guest VMs available for task allocation");
		}

		try {
			// Initialize cloudlet parameters
			long fileSize = NetworkConstants.FILE_SIZE;
			long outputSize = NetworkConstants.OUTPUT_SIZE;
			
			if (fileSize <= 0 || outputSize <= 0) {
				throw new IllegalArgumentException("File size and output size must be positive values");
			}

			UtilizationModel utilizationModel = new UtilizationModelFull();

			// Create first cloudlet (cla)
			NetworkCloudlet cla = createNetworkCloudlet(
				NetworkConstants.currentCloudletId++,
				fileSize,
				outputSize,
				utilizationModel,
				broker.getId(),
				containerList.get(0).getId()
			);

			if (cla == null) {
				throw new RuntimeException("Failed to create first cloudlet (cla)");
			}
			appCloudlet.cList.add(cla);

			// Create second cloudlet (clb)
			NetworkCloudlet clb = createNetworkCloudlet(
				NetworkConstants.currentCloudletId++,
				fileSize,
				outputSize,
				utilizationModel,
				broker.getId(),
				guestList.get(0).getId()
			);

			if (clb == null) {
				throw new RuntimeException("Failed to create second cloudlet (clb)");
			}
			appCloudlet.cList.add(clb);

			// Configure task stages
			Log.printlnConcat("Configuring task stages for cloudlets...");
			
			// Configure cla stages
			cla.addExecutionStage(1000);
			cla.addSendStage(1000, clb);
			Log.printlnConcat("Configured stages for cloudlet #", cla.getCloudletId());

			// Configure clb stages
			clb.addRecvStage(cla);
			clb.addExecutionStage(1000);
			Log.printlnConcat("Configured stages for cloudlet #", clb.getCloudletId());

			Log.printlnConcat("Successfully created and configured ", appCloudlet.cList.size(), " cloudlets");

		} catch (Exception e) {
			String errorMsg = "Error creating task list: " + e.getMessage();
			Log.println(errorMsg);
			throw new RuntimeException(errorMsg, e);
		}
	}

	/**
	 * Helper method to create a NetworkCloudlet with the specified parameters.
	 */
	private static NetworkCloudlet createNetworkCloudlet(
			int cloudletId,
			long fileSize,
			long outputSize,
			UtilizationModel utilizationModel,
			int userId,
			int guestId) {
		
		NetworkCloudlet cloudlet = new NetworkCloudlet(
			cloudletId,
			0,  // pesNumber
			1,  // cloudletLength
			fileSize,
			outputSize,
			utilizationModel,
			utilizationModel,
			utilizationModel
		);

		cloudlet.setUserId(userId);
		cloudlet.setGuestId(guestId);

		return cloudlet;
	}

	private static void CreateNetwork(NetworkDatacenter dc) {
		if (dc == null) {
			throw new IllegalArgumentException("NetworkDatacenter cannot be null");
		}

		try {
			// Create ToR switch with validated parameters
			if (NetworkConstants.EdgeSwitchPort <= 0) {
				throw new IllegalArgumentException("Switch port count must be positive");
			}

			Switch ToRSwitch = new Switch(
				"Edge0",
				NetworkConstants.EdgeSwitchPort,
				Switch.SwitchLevel.EDGE_LEVEL,
				0,
				NetworkConstants.BandWidthEdgeHost,
				NetworkConstants.BandWidthEdgeAgg,
				dc);

			if (ToRSwitch == null) {
				throw new RuntimeException("Failed to create Top of Rack switch");
			}

			// Register the switch with the datacenter
			dc.registerSwitch(ToRSwitch);
			Log.printlnConcat("Successfully registered switch Edge0 with datacenter");

			// Get the list of hosts from the datacenter
			List<NetworkHost> netHosts = dc.getHostList();
			if (netHosts == null || netHosts.isEmpty()) {
				throw new RuntimeException("No hosts available in datacenter for network configuration");
			}

			// Attach hosts to the switch
			int attachedHosts = 0;
			for (NetworkHost netHost : netHosts) {
				if (netHost != null) {
					try {
						dc.attachSwitchToHost(ToRSwitch, netHost);
						attachedHosts++;
						Log.printlnConcat("Successfully attached host #", netHost.getId(), " to switch Edge0");
					} catch (Exception e) {
						Log.printlnConcat("Error attaching host #", netHost.getId(), " to switch: ", e.getMessage());
					}
				}
			}

			if (attachedHosts == 0) {
				throw new RuntimeException("Failed to attach any hosts to the network switch");
			}

			Log.printlnConcat("Network created successfully with ", attachedHosts, " hosts attached");

		} catch (Exception e) {
			String errorMsg = "Error creating network: " + e.getMessage();
			Log.println(errorMsg);
			throw new RuntimeException(errorMsg, e);
		}
	}
}
