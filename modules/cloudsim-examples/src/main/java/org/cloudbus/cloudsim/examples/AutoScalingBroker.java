package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * A broker that implements auto-scaling functionality.
 * It dynamically adds VMs based on workload.
 */
public class AutoScalingBroker extends DatacenterBroker {
    // Auto-scaling configuration
    private final int initVmCount;
    private final int maxVmCount;
    private final int cloudletsPerVm;
    
    // VM counter
    private int vmIdCounter;
    
    // VM template parameters
    private int mips;
    private int pesNumber;
    private int ram;
    private long bw;
    private long size;
    private String vmm;
    
    /**
     * Creates a new broker.
     *
     * @param name the broker name
     * @param initVmCount initial number of VMs
     * @param maxVmCount maximum number of VMs
     * @param cloudletsPerVm target number of cloudlets per VM
     * @throws Exception the exception
     */
    public AutoScalingBroker(String name, int initVmCount, int maxVmCount, int cloudletsPerVm) throws Exception {
        super(name);
        this.initVmCount = initVmCount;
        this.maxVmCount = maxVmCount;
        this.cloudletsPerVm = cloudletsPerVm;
        this.vmIdCounter = initVmCount; // Start VM IDs after the initial VMs
    }
    
    /**
     * Sets the VM parameters to use for scaling.
     *
     * @param mips the MIPS
     * @param pesNumber the number of PEs
     * @param ram the RAM
     * @param bw the bandwidth
     * @param size the storage size
     * @param vmm the VMM name
     */
    public void setVmParameters(int mips, int pesNumber, int ram, long bw, long size, String vmm) {
        this.mips = mips;
        this.pesNumber = pesNumber;
        this.ram = ram;
        this.bw = bw;
        this.size = size;
        this.vmm = vmm;
    }
    
    /**
     * Checks if auto-scaling is needed and creates new VMs if necessary.
     * This is called manually from the main simulation.
     */
    public void checkAndScaleUp() {
        List<Cloudlet> cloudlets = getCloudletList();
        List<?> vms = getGuestList();
        
        int totalVms = vms.size();
        int unassignedCloudlets = 0;
        
        // Count unassigned cloudlets
        for (Cloudlet cloudlet : cloudlets) {
            if (cloudlet.getGuestId() == -1) {
                unassignedCloudlets++;
            }
        }
        
        Log.printLine(CloudSim.clock() + ": AutoScalingBroker: Checking scaling - " 
                + totalVms + " VMs, " + cloudlets.size() + " total cloudlets, " 
                + unassignedCloudlets + " unassigned");
        
        // Calculate current utilization ratio (cloudlets per VM)
        double currentRatio = (double) cloudlets.size() / Math.max(1, totalVms);
        double targetRatio = cloudletsPerVm;
        
        // Scale up if needed
        if ((unassignedCloudlets > 0 || currentRatio > targetRatio * 0.7)
                && totalVms < maxVmCount) {
                
            int vmsToCreate = Math.min(
                (int) Math.ceil((double) unassignedCloudlets / cloudletsPerVm),
                maxVmCount - totalVms
            );
            
            if (vmsToCreate > 0) {
                Log.printLine(CloudSim.clock() + ": AutoScalingBroker: Scaling UP - Creating " 
                        + vmsToCreate + " new VMs");
                createAndSubmitVms(vmsToCreate);
            }
        }
    }
    
    /**
     * Creates and submits new VMs.
     *
     * @param count the number of VMs to create
     */
    private void createAndSubmitVms(int count) {
        List<Vm> newVms = new ArrayList<Vm>();
        
        for (int i = 0; i < count; i++) {
            Vm vm = new Vm(
                    vmIdCounter++, 
                    getId(),
                    mips,
                    pesNumber,
                    ram,
                    bw,
                    size,
                    vmm,
                    new CloudletSchedulerTimeShared()
            );
            
            newVms.add(vm);
        }
        
        // Submit VMs to the datacenter
        submitGuestList(newVms);
    }
    
    /**
     * A utilization model that provides variable utilization levels.
     */
    public static class VariableUtilizationModel implements UtilizationModel {
        private double utilizationRatio;
        
        public VariableUtilizationModel(double initialRatio) {
            this.utilizationRatio = initialRatio;
        }
        
        public void setUtilizationRatio(double ratio) {
            this.utilizationRatio = ratio;
        }
        
        @Override
        public double getUtilization(double time) {
            return utilizationRatio;
        }
    }
} 