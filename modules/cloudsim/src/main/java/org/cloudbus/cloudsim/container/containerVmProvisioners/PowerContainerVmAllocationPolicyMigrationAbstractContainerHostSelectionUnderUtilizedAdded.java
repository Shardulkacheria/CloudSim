package org.cloudbus.cloudsim.container.containerVmProvisioners;

import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.Host;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public abstract class PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelectionUnderUtilizedAdded 
    extends PowerContainerVmAllocationPolicyMigrationAbstract {

    public PowerContainerVmAllocationPolicyMigrationAbstractContainerHostSelectionUnderUtilizedAdded(List<? extends Host> hostList) {
        super(hostList);
    }

    @Override
    protected Map<String, Object> findHostForVm() {
        Map<String, Object> result = new HashMap<>();
        // Implementation details to be added by concrete classes
        return result;
    }

    protected Host findHostForVmUnderUtilized(List<Host> underUtilizedHostList) {
        if (!underUtilizedHostList.isEmpty()) {
            return underUtilizedHostList.get(0);
        }
        return null;
    }
}