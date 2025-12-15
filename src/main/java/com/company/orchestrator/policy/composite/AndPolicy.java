package com.company.orchestrator.policy.composite;

import com.company.orchestrator.policy.*;
import java.util.List;

public final class AndPolicy implements CompositePolicy {

    private final List<Policy> policies;

    public AndPolicy(List<Policy> policies) {
        this.policies = List.copyOf(policies);
    }

    @Override
    public PolicyEvaluationResult evaluate(PolicyContext context) {
        for (Policy policy : policies) {
            var result = policy.evaluate(context);
            if (!result.allowed()) {
                return result; // short-circuit
            }
        }
        return PolicyEvaluationResult.allow();
    }
}
