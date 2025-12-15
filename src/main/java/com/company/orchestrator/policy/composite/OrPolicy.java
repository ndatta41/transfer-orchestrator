package com.company.orchestrator.policy.composite;

import com.company.orchestrator.policy.*;
import java.util.List;

public final class OrPolicy implements CompositePolicy {

    private final List<Policy> policies;

    public OrPolicy(List<Policy> policies) {
        this.policies = List.copyOf(policies);
    }

    @Override
    public PolicyEvaluationResult evaluate(PolicyContext context) {
        for (Policy policy : policies) {
            if (policy.evaluate(context).allowed()) {
                return PolicyEvaluationResult.allow();
            }
        }
        return PolicyEvaluationResult.deny(
                "None of the OR-composed policies were satisfied"
        );
    }
}
