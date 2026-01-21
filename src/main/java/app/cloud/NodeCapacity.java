package app.cloud;

public record NodeCapacity(String nodeId, int cpuCores, int memoryGb) {
    public boolean canFit(Workload workload) {
        return cpuCores >= workload.cpuCores() && memoryGb >= workload.memoryGb();
    }

    public NodeCapacity allocate(Workload workload) {
        return new NodeCapacity(nodeId, cpuCores - workload.cpuCores(), memoryGb - workload.memoryGb());
    }
}
