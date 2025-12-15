package com.company.orchestrator.policy.atomic;

import com.company.orchestrator.policy.*;

public final class RateLimitPolicy implements AtomicPolicy {

    private final long maxRequestsPerHour;

    public RateLimitPolicy(long maxRequestsPerHour) {
        this.maxRequestsPerHour = maxRequestsPerHour;
    }

    @Override
    public PolicyEvaluationResult evaluate(PolicyContext context) {
        if (context.requestsInLastHour() > maxRequestsPerHour) {
            return PolicyEvaluationResult.deny(
                    "Rate limit exceeded: max " + maxRequestsPerHour + " requests/hour"
            );
        }
        return PolicyEvaluationResult.allow();
    }
}
