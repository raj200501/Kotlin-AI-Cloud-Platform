package app.cloud;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResourceAllocator {
    public ClusterPlan allocate(List<NodeCapacity> nodes, List<Workload> workloads) {
        List<NodeCapacity> capacities = new ArrayList<>(nodes);
        Map<String, String> placements = new LinkedHashMap<>();
        for (Workload workload : workloads) {
            boolean placed = false;
            for (int i = 0; i < capacities.size(); i++) {
                NodeCapacity capacity = capacities.get(i);
                if (capacity.canFit(workload)) {
                    placements.put(workload.workloadId(), capacity.nodeId());
                    capacities.set(i, capacity.allocate(workload));
                    placed = true;
                    break;
                }
            }
            if (!placed) {
                placements.put(workload.workloadId(), "unassigned");
            }
        }
        return new ClusterPlan(capacities, placements);
    }

    public double utilization(NodeCapacity node, NodeCapacity remaining) {
        double cpuUsed = node.cpuCores() - remaining.cpuCores();
        double memUsed = node.memoryGb() - remaining.memoryGb();
        double cpuUtil = node.cpuCores() == 0 ? 0 : cpuUsed / node.cpuCores();
        double memUtil = node.memoryGb() == 0 ? 0 : memUsed / node.memoryGb();
        return Math.max(cpuUtil, memUtil);
    }
}
