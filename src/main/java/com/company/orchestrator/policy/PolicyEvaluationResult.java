package com.company.orchestrator.policy;

public record PolicyEvaluationResult(
        boolean allowed,
        String violationReason
) {
    public static PolicyEvaluationResult allow() {
        return new PolicyEvaluationResult(true, null);
    }

    public static PolicyEvaluationResult deny(String reason) {
        return new PolicyEvaluationResult(false, reason);
    }
}
