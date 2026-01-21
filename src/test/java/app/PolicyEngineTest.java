package app;

import app.cloud.Workload;
import app.policy.PolicyDecision;
import app.policy.PolicyEngine;
import app.policy.RiskLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyEngineTest {
    @Test
    void flagsRestrictedTier() {
        PolicyEngine engine = new PolicyEngine();
        PolicyDecision decision = engine.evaluate(new Workload("w1", 2, 4, "restricted"));
        assertFalse(decision.allowed());
        assertEquals(RiskLevel.HIGH, decision.riskLevel());
    }

    @Test
    void allowsStandardWorkload() {
        PolicyEngine engine = new PolicyEngine();
        PolicyDecision decision = engine.evaluate(new Workload("w1", 4, 8, "standard"));
        assertTrue(decision.allowed());
        assertEquals(RiskLevel.LOW, decision.riskLevel());
    }
}
