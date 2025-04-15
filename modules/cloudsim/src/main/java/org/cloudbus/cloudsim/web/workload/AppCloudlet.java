// ... existing code ...
if (networkCloudlet.getStagesList().get(0).getType() == NetworkConstants.EXECUTION) {
    networkCloudlet.getStagesList().get(0).setLength(this.getActualCpuTime());
}
if (networkCloudlet.getStagesList().get(networkCloudlet.getStagesList().size() - 1).getType() == NetworkConstants.EXECUTION) {
    networkCloudlet.getStagesList().get(networkCloudlet.getStagesList().size() - 1).setLength(this.getActualCpuTime());
}
// ... existing code ...