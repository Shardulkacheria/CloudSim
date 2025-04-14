package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * An example of an auto-scaling simulation based on load
 * It demonstrates adding VMs as the number of cloudlets increases
 */
public class AutoScalingExample {

    /** The cloudlet list. */
    private static List<Cloudlet> cloudletList;

    /** The vm list. */
    private static List<Vm> vmList;
    
    /** The initial number of VMs to create. */
    private static final int INIT_VM_COUNT = 2;
    
    /** The maximum number of VMs that can be created. */
    private static final int MAX_VM_COUNT = 10;
    
    /** The cloudlets per VM ratio that defines when to scale. */
    private static final int CLOUDLETS_PER_VM = 3;

    /**
     * Creates main() to run this example.
     *
     * @param args the args
     */
    public static void main(String[] args) {
        Log.printLine("Starting AutoScalingExample...");

        try {
            // Number of cloud users
            int num_user = 1;
            
            // Calendar - starting time of the simulation
            Calendar calendar = Calendar.getInstance();
            
            // Initialize the CloudSim library
            boolean trace_flag = false;  // mean trace events
            CloudSim.init(num_user, calendar, trace_flag);

            // Create Datacenters
            Datacenter datacenter0 = createDatacenter("Datacenter_0");

            // Create Auto-Scaling Broker
            AutoScalingBroker broker = createBroker();
            int brokerId = broker.getId();

            // VM Properties
            int vmid = 0;
            int mips = 1000;
            long size = 10000; // image size (MB)
            int ram = 512; // vm memory (MB)
            long bw = 1000;
            int pesNumber = 1; // number of cpus
            String vmm = "Xen"; // VMM name
            
            // Set VM parameters for auto-scaling
            broker.setVmParameters(mips, pesNumber, ram, bw, size, vmm);

            // Create initial VMs
            vmList = new ArrayList<>();
            for (int i = 0; i < INIT_VM_COUNT; i++) {
                Vm vm = new Vm(vmid++, brokerId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
                vmList.add(vm);
            }

            // Submit VM list to the broker
            broker.submitGuestList(vmList);

            // Cloudlet properties
            int id = 0;
            long length = 10000;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();

            cloudletList = new ArrayList<>();

            // Create initial cloudlets
            for (int i = 0; i < INIT_VM_COUNT * CLOUDLETS_PER_VM; i++) {
                Cloudlet cloudlet = new Cloudlet(id++, length, pesNumber, fileSize, outputSize, 
                                              utilizationModel, utilizationModel, utilizationModel);
                cloudlet.setUserId(brokerId);
                // Round-robin assignment of cloudlets to VMs
                cloudlet.setGuestId(i % INIT_VM_COUNT);
                cloudletList.add(cloudlet);
            }

            // Submit initial cloudlet list to the broker
            broker.submitCloudletList(cloudletList);
            
            // Start the simulation
            CloudSim.startSimulation();
            
            // First phase of simulation
            Log.printLine("PHASE 1: Initial load with " + cloudletList.size() + " cloudlets on " + INIT_VM_COUNT + " VMs");
            
            // Add more cloudlets to test auto-scaling
            int newCloudletsCount = 5;
            List<Cloudlet> newCloudlets = new ArrayList<>();
            
            for (int i = 0; i < newCloudletsCount; i++) {
                Cloudlet cloudlet = new Cloudlet(id++, length, pesNumber, fileSize, outputSize, 
                                              utilizationModel, utilizationModel, utilizationModel);
                cloudlet.setUserId(brokerId);
                // We'll leave VM assignment to the broker
                newCloudlets.add(cloudlet);
            }
            
            // Check and activate auto-scaling based on new load
            broker.checkAndScaleUp();
            
            // Submit new cloudlets
            broker.submitCloudletList(newCloudlets);
            cloudletList.addAll(newCloudlets);
            
            Log.printLine("PHASE 2: Added " + newCloudletsCount + " more cloudlets and checked auto-scaling");
            
            // Pause to let the auto-scaling take effect
            CloudSim.pauseSimulation(100);
            CloudSim.resumeSimulation();
            
            // Add even more cloudlets to test further scaling
            int moreCloudletsCount = 10;
            List<Cloudlet> moreCloudlets = new ArrayList<>();
            
            for (int i = 0; i < moreCloudletsCount; i++) {
                Cloudlet cloudlet = new Cloudlet(id++, length, pesNumber, fileSize, outputSize, 
                                             utilizationModel, utilizationModel, utilizationModel);
                cloudlet.setUserId(brokerId);
                moreCloudlets.add(cloudlet);
            }
            
            // Activate auto-scaling again with new load
            broker.checkAndScaleUp();
            
            // Submit more cloudlets
            broker.submitCloudletList(moreCloudlets);
            cloudletList.addAll(moreCloudlets);
            
            Log.printLine("PHASE 3: Added " + moreCloudletsCount + " more cloudlets and checked auto-scaling again");
            
            // Finish the simulation
            CloudSim.stopSimulation();

            // Print results
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            printCloudletList(newList);
            
            // Print scaling statistics
            Log.printLine("\n========== AUTO-SCALING STATISTICS ==========");
            Log.printLine("Initial VMs: " + INIT_VM_COUNT);
            Log.printLine("Final VMs: " + broker.getGuestList().size());
            Log.printLine("Total Cloudlets: " + cloudletList.size());
            Log.printLine("Cloudlets per VM ratio: " + String.format("%.2f", (float)cloudletList.size() / broker.getGuestList().size()));

            Log.printLine("AutoScalingExample finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    /**
     * Creates the broker.
     *
     * @return the datacenter broker
     */
    private static AutoScalingBroker createBroker() {
        AutoScalingBroker broker = null;
        try {
            broker = new AutoScalingBroker("AutoScalingBroker", INIT_VM_COUNT, MAX_VM_COUNT, CLOUDLETS_PER_VM);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }

    /**
     * Creates the datacenter.
     *
     * @param name the name
     *
     * @return the datacenter
     */
    private static Datacenter createDatacenter(String name) {
        // Create a list to store our machine
        List<Host> hostList = new ArrayList<Host>();

        // Machine specifications
        int hostId = 0;
        int ram = 16384; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;
        int mips = 2000; // Million Instructions per Second
        int hostPesNumber = 8; // number of CPUs

        // Create PEs and add to list
        List<Pe> peList = new ArrayList<Pe>();
        for (int i = 0; i < hostPesNumber; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
        }

        // Create Host with its id and list of PEs and add them to the list of machines
        hostList.add(
                new Host(
                        hostId,
                        new RamProvisionerSimple(ram),
                        new BwProvisionerSimple(bw),
                        storage,
                        peList,
                        new VmSchedulerTimeShared(peList)
                )
        );

        // Create a DatacenterCharacteristics object
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        // Create a Datacenter
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    /**
     * Prints the Cloudlet objects.
     *
     * @param list list of Cloudlets
     */
    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();
        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent + "Time" + indent
                + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId()
                        + indent + indent + indent + cloudlet.getGuestId()
                        + indent + indent
                        + dft.format(cloudlet.getActualCPUTime()) + indent
                        + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
        }
    }
} 