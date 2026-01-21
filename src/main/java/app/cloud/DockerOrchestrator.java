package app.cloud;

import java.util.ArrayList;
import java.util.List;

public final class DockerOrchestrator {
    public List<String> planContainers(List<Workload> workloads) {
        List<String> commands = new ArrayList<>();
        for (Workload workload : workloads) {
            String command = "docker run -d --name " + workload.workloadId() +
                    " --cpus=" + workload.cpuCores() +
                    " --memory=" + workload.memoryGb() + "g example/" + workload.workloadId() + ":latest";
            commands.add(command);
        }
        return commands;
    }
}
