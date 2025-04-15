package org.cloudbus.cloudsim.container.containerVmProvisioners;

import org.cloudbus.cloudsim.container.core.PowerContainerHost;
import java.util.List;

public abstract class PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelectionUnderUtilizedAdded 
    extends PowerContainerVmAllocationPolicyMigrationAbstract {

    protected PowerContainerHost findHostForVm(List<PowerContainerHost> switchedOffHosts) {
        if (!switchedOffHosts.isEmpty()) {
            PowerContainerHost host = switchedOffHosts.get(switchedOffHosts.size()-1);
            return host;
        }
        return null;
    }
}