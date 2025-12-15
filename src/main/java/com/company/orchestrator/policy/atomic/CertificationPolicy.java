package com.company.orchestrator.policy.atomic;

import com.company.orchestrator.policy.*;

public final class CertificationPolicy implements AtomicPolicy {

    private final String requiredCertification;

    public CertificationPolicy(String requiredCertification) {
        this.requiredCertification = requiredCertification;
    }

    @Override
    public PolicyEvaluationResult evaluate(PolicyContext context) {
        if (!context.consumerCertifications().contains(requiredCertification)) {
            return PolicyEvaluationResult.deny(
                    "Missing required certification: " + requiredCertification
            );
        }
        return PolicyEvaluationResult.allow();
    }
}
