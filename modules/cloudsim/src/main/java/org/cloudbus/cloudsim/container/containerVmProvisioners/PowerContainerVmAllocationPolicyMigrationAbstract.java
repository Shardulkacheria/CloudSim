package org.cloudbus.cloudsim.container.containerVmProvisioners;

import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.Host;
import java.util.List;
import java.util.Map;

public abstract class PowerContainerVmAllocationPolicyMigrationAbstract extends VmAllocationPolicy {
    
    public PowerContainerVmAllocationPolicyMigrationAbstract(List<? extends Host> hostList) {
        super(hostList);
    }

    protected abstract Map<String, Object> findHostForVm();
} 