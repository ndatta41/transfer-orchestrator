package com.company.orchestrator.policy.atomic;

import com.company.orchestrator.policy.*;

public final class UsagePolicy implements AtomicPolicy {

    private final String allowedPurpose;

    public UsagePolicy(String allowedPurpose) {
        this.allowedPurpose = allowedPurpose;
    }

    @Override
    public PolicyEvaluationResult evaluate(PolicyContext context) {
        if (!allowedPurpose.equalsIgnoreCase(context.usagePurpose())) {
            return PolicyEvaluationResult.deny(
                    "Usage purpose not allowed: " + context.usagePurpose()
            );
        }
        return PolicyEvaluationResult.allow();
    }
}
