package com.company.orchestrator.policy;

public sealed interface Policy
        permits AtomicPolicy, CompositePolicy {

    PolicyEvaluationResult evaluate(PolicyContext context);
}
