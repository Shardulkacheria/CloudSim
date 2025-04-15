/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2024, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.network.datacenter;

/**
 * TaskStage represents various stages a {@link NetworkCloudlet} can have during execution. 
 * Four stage types which are possible: EXECUTION, WAIT_SEND, WAIT_RECV,and FINISH.
 * 
 * <br/>Please refer to following publication for more details:<br/>
 * <ul>
 * <li><a href="http://dx.doi.org/10.1109/UCC.2011.24">Saurabh Kumar Garg and Rajkumar Buyya, NetworkCloudSim: Modelling Parallel Applications in Cloud
 * Simulations, Proceedings of the 4th IEEE/ACM International Conference on Utility and Cloud
 * Computing (UCC 2011, IEEE CS Press, USA), Melbourne, Australia, December 5-7, 2011.</a>
 * </ul>
 * 
 * @author Saurabh Kumar Garg
 * @author Remo Andreoli
 * @since CloudSim Toolkit 1.0
 */
public class TaskStage {
	public enum TaskStageStatus {
		EXECUTION,
		WAIT_SEND,
		WAIT_RECV,
		FINISH;
	}

	private TaskStageStatus type;
	private double length;
	private double processingTime;
	private final double stageId;
	private NetworkCloudlet targetCloudlet;
	
	public TaskStage(TaskStageStatus type, double length, double stageId, NetworkCloudlet cl) {
		this.type = type;
		this.length = length;
		this.processingTime = 0;
		this.stageId = stageId;
		this.targetCloudlet = cl;
	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getProcessingTime() {
		return processingTime;
	}

	public void setProcessingTime(double processingTime) {
		this.processingTime = processingTime;
	}

	public TaskStageStatus getType() {
		return type;
	}

	public double getStageId() { 
		return stageId; 
	}

	public NetworkCloudlet getTargetCloudlet() { 
		return targetCloudlet; 
	}

	public double getTaskLength() {
		return length;
	}

	public void setTime(double time) {
		this.processingTime = time;
	}

	public double getTime() {
		return processingTime;
	}
}
