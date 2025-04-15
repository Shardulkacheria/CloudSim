package org.cloudbus.cloudsim.web.workload;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.network.datacenter.NetworkCloudlet;
import org.cloudbus.cloudsim.network.datacenter.TaskStage;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an application cloudlet that can contain multiple network cloudlets
 * and manages their execution stages.
 */
public class AppCloudlet {
    public static final int APP_Workflow = 1;
    
    private int appID;
    private int userID;
    private int deadline;
    public List<NetworkCloudlet> cList;

    /**
     * Constructor for creating a new AppCloudlet.
     *
     * @param type The type of the application
     * @param appID The ID of the application
     * @param deadline The deadline for execution
     * @param userID The ID of the user who submitted this cloudlet
     */
    public AppCloudlet(int type, int appID, int deadline, int userID) {
        this.appID = appID;
        this.deadline = deadline;
        this.userID = userID;
        this.cList = new ArrayList<>();
    }

    /**
     * Gets the lateness of the application execution.
     *
     * @return The lateness value (how much it exceeded the deadline)
     */
    public double getLateness() {
        if (cList.isEmpty()) return 0;
        
        NetworkCloudlet lastCloudlet = cList.get(cList.size() - 1);
        return Math.max(0, lastCloudlet.getFinishTime() - deadline);
    }

    /**
     * Updates the execution stages of network cloudlets by recreating them
     * with the current cloudlet length.
     *
     * @param networkCloudlet The network cloudlet to update
     */
    public void updateExecutionStages(NetworkCloudlet networkCloudlet) {
        // Clear existing stages
        networkCloudlet.stages.clear();
        
        // Add a new execution stage with the current length
        networkCloudlet.addExecutionStage(networkCloudlet.getCloudletLength());
    }

    // Getters and setters
    public int getAppID() {
        return appID;
    }

    public void setAppID(int appID) {
        this.appID = appID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getDeadline() {
        return deadline;
    }

    public void setDeadline(int deadline) {
        this.deadline = deadline;
    }
}