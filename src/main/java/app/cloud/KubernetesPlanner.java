package app.cloud;

import java.util.ArrayList;
import java.util.List;

public final class KubernetesPlanner {
    public List<String> planDeployments(List<Workload> workloads) {
        List<String> manifests = new ArrayList<>();
        for (Workload workload : workloads) {
            String manifest = "apiVersion: apps/v1\n" +
                    "kind: Deployment\n" +
                    "metadata:\n" +
                    "  name: " + workload.workloadId() + "\n" +
                    "spec:\n" +
                    "  replicas: 1\n" +
                    "  selector:\n" +
                    "    matchLabels:\n" +
                    "      app: " + workload.workloadId() + "\n" +
                    "  template:\n" +
                    "    metadata:\n" +
                    "      labels:\n" +
                    "        app: " + workload.workloadId() + "\n" +
                    "    spec:\n" +
                    "      containers:\n" +
                    "        - name: " + workload.workloadId() + "\n" +
                    "          image: example/" + workload.workloadId() + ":latest\n" +
                    "          resources:\n" +
                    "            requests:\n" +
                    "              cpu: \"" + workload.cpuCores() + "\"\n" +
                    "              memory: \"" + workload.memoryGb() + "Gi\"\n";
            manifests.add(manifest);
        }
        return manifests;
    }
}
