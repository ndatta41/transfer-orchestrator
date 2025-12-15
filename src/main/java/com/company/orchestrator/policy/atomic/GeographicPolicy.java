package com.company.orchestrator.policy.atomic;

import com.company.orchestrator.policy.*;
import java.util.Set;

public final class GeographicPolicy implements AtomicPolicy {

    private static final Set<String> EU_REGIONS =
            Set.of("EU", "DE", "FR", "NL", "IT", "ES");

    @Override
    public PolicyEvaluationResult evaluate(PolicyContext context) {
        if (!EU_REGIONS.contains(context.consumerRegion())) {
            return PolicyEvaluationResult.deny(
                    "Data transfer outside EU region is not permitted"
            );
        }
        return PolicyEvaluationResult.allow();
    }
}
