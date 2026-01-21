package app.cloud;

public record Workload(String workloadId, int cpuCores, int memoryGb, String tier) {}
