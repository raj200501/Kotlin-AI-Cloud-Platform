package app.cloud;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record ClusterPlan(List<NodeCapacity> remainingCapacity, Map<String, String> placements) {
    public int totalPlacements() {
        return placements.size();
    }

    public List<NodeCapacity> remainingCapacity() {
        return Collections.unmodifiableList(remainingCapacity);
    }

    public Map<String, String> placements() {
        return Collections.unmodifiableMap(placements);
    }
}
