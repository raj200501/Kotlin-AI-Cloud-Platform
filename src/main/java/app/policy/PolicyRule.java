package app.policy;

import app.cloud.Workload;

public interface PolicyRule {
    PolicyDecision evaluate(Workload workload);

    static PolicyDecision allowed(String reason) {
        return new PolicyDecision(RiskLevel.LOW, true, java.util.List.of(reason));
    }

    static PolicyDecision denied(RiskLevel level, String reason) {
        return new PolicyDecision(level, false, java.util.List.of(reason));
    }
}
