package app.policy;

import app.cloud.Workload;
import java.util.ArrayList;
import java.util.List;

public final class PolicyEngine {
    private final List<PolicyRule> rules = new ArrayList<>();

    public PolicyEngine() {
        rules.add(new CpuLimitRule());
        rules.add(new MemoryLimitRule());
        rules.add(new TierRule());
    }

    public PolicyDecision evaluate(Workload workload) {
        List<String> reasons = new ArrayList<>();
        RiskLevel highest = RiskLevel.LOW;
        for (PolicyRule rule : rules) {
            PolicyDecision decision = rule.evaluate(workload);
            reasons.addAll(decision.reasons());
            if (decision.riskLevel().ordinal() > highest.ordinal()) {
                highest = decision.riskLevel();
            }
            if (!decision.allowed()) {
                return new PolicyDecision(highest, false, reasons);
            }
        }
        return new PolicyDecision(highest, true, reasons);
    }

    private static final class CpuLimitRule implements PolicyRule {
        @Override
        public PolicyDecision evaluate(Workload workload) {
            if (workload.cpuCores() > 32) {
                return PolicyRule.denied(RiskLevel.HIGH, "cpu cores exceed policy limit");
            }
            if (workload.cpuCores() > 16) {
                return new PolicyDecision(RiskLevel.MEDIUM, true, List.of("high cpu workload"));
            }
            return PolicyRule.allowed("cpu within policy");
        }
    }

    private static final class MemoryLimitRule implements PolicyRule {
        @Override
        public PolicyDecision evaluate(Workload workload) {
            if (workload.memoryGb() > 64) {
                return PolicyRule.denied(RiskLevel.HIGH, "memory exceeds policy limit");
            }
            if (workload.memoryGb() > 32) {
                return new PolicyDecision(RiskLevel.MEDIUM, true, List.of("high memory workload"));
            }
            return PolicyRule.allowed("memory within policy");
        }
    }

    private static final class TierRule implements PolicyRule {
        @Override
        public PolicyDecision evaluate(Workload workload) {
            if ("restricted".equalsIgnoreCase(workload.tier())) {
                return PolicyRule.denied(RiskLevel.HIGH, "restricted tier requires approval");
            }
            return PolicyRule.allowed("tier allowed");
        }
    }
}
