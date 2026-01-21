package app;

import app.cloud.ClusterPlan;
import app.cloud.NodeCapacity;
import app.cloud.ResourceAllocator;
import app.cloud.Workload;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceAllocatorTest {
    @Test
    void allocatesWorkloadsGreedily() {
        ResourceAllocator allocator = new ResourceAllocator();
        List<NodeCapacity> nodes = List.of(
                new NodeCapacity("n1", 4, 8),
                new NodeCapacity("n2", 8, 16)
        );
        List<Workload> workloads = List.of(
                new Workload("w1", 2, 4, "standard"),
                new Workload("w2", 6, 8, "premium")
        );
        ClusterPlan plan = allocator.allocate(nodes, workloads);
        assertEquals("n1", plan.placements().get("w1"));
        assertEquals("n2", plan.placements().get("w2"));
        assertTrue(plan.remainingCapacity().get(1).cpuCores() <= 2);
    }

    @Test
    void marksUnassignedWhenCapacityIsInsufficient() {
        ResourceAllocator allocator = new ResourceAllocator();
        List<NodeCapacity> nodes = List.of(new NodeCapacity("n1", 1, 1));
        List<Workload> workloads = List.of(new Workload("w1", 2, 2, "standard"));
        ClusterPlan plan = allocator.allocate(nodes, workloads);
        assertEquals("unassigned", plan.placements().get("w1"));
    }
}
