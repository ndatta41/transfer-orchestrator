package com.company.orchestrator.policy.composite;

import com.company.orchestrator.policy.*;

public final class NotPolicy implements CompositePolicy {

    private final Policy policy;

    public NotPolicy(Policy policy) {
        this.policy = policy;
    }

    @Override
    public PolicyEvaluationResult evaluate(PolicyContext context) {
        var result = policy.evaluate(context);
        return result.allowed()
                ? PolicyEvaluationResult.deny("NOT policy violation")
                : PolicyEvaluationResult.allow();
    }
}
