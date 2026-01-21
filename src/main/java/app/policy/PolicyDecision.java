package app.policy;

import java.util.List;

public record PolicyDecision(RiskLevel riskLevel, boolean allowed, List<String> reasons) {}
