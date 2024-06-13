/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.core.HostEntity;

/**
 * An abstract power-aware VM allocation policy.
 * 
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 * 
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley &amp; Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 * 
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 3.0
 */
public abstract class PowerVmAllocationPolicyAbstract extends VmAllocationPolicy {

	/** The map map where each key is a VM id and
         * each value is the host where the VM is placed. */
	private final Map<String, HostEntity> guestTable = new HashMap<>();

	/**
	 * Instantiates a new PowerVmAllocationPolicyAbstract.
	 * 
	 * @param list the list
	 */
	public PowerVmAllocationPolicyAbstract(List<? extends HostEntity> list) {
		super(list);
	}

	@Override
	public boolean allocateHostForGuest(GuestEntity guest) {
		return allocateHostForGuest(guest, findHostForGuest(guest));
	}

	@Override
	public boolean allocateHostForGuest(GuestEntity guest, HostEntity host) {
		if (host == null) {
			Log.formatLine("%.2f: No suitable host found for "+guest.getClassName()+" #" + guest.getId() + "\n", CloudSim.clock());
			return false;
		}
		if (host.guestCreate(guest)) { // if vm has been succesfully created in the host
			getGuestTable().put(guest.getUid(), host);
			Log.formatLine(
					"%.2f: "+guest.getClassName()+" #" + guest.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			return true;
		}
		Log.formatLine(
				"%.2f: Creation of "+guest.getClassName()+" #" + guest.getId() + " on the host #" + host.getId() + " failed\n",
				CloudSim.clock());
		return false;
	}

	@Override
	public void deallocateHostForGuest(GuestEntity guest) {
		HostEntity host = getGuestTable().remove(guest.getUid());
		if (host != null) {
			host.guestDestroy(guest);
		}
	}

	@Override
	public HostEntity getHost(GuestEntity guest) {
		return getGuestTable().get(guest.getUid());
	}

	@Override
	public HostEntity getHost(int vmId, int userId) {
		return getGuestTable().get(Vm.getUid(userId, vmId));
	}

	/**
	 * Gets the vm table.
	 * 
	 * @return the vm table
	 */
	public Map<String, HostEntity> getGuestTable() {
		return guestTable;
	}

}
