package app.policy;

import app.cloud.Workload;
import java.util.List;

public final class PolicyEnforcer {
    private static final String ENABLED_ENV = "ENABLE_POLICY";
    private final PolicyEngine engine = new PolicyEngine();

    public boolean isEnabled() {
        return "1".equals(System.getenv(ENABLED_ENV));
    }

    public PolicyDecision evaluate(List<Workload> workloads) {
        RiskLevel highest = RiskLevel.LOW;
        boolean allowed = true;
        java.util.List<String> reasons = new java.util.ArrayList<>();
        for (Workload workload : workloads) {
            PolicyDecision decision = engine.evaluate(workload);
            reasons.addAll(decision.reasons());
            if (!decision.allowed()) {
                allowed = false;
            }
            if (decision.riskLevel().ordinal() > highest.ordinal()) {
                highest = decision.riskLevel();
            }
        }
        return new PolicyDecision(highest, allowed, reasons);
    }
}
