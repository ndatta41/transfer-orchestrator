package com.company.orchestrator.domain.service;

import com.company.orchestrator.policy.Policy;
import com.company.orchestrator.policy.PolicyContext;
import com.company.orchestrator.policy.PolicyEvaluationResult;
import org.springframework.stereotype.Service;

@Service
public class PolicyEvaluationService {

    public PolicyEvaluationResult evaluate(Policy policy, PolicyContext ctx) {
        PolicyEvaluationResult result = policy.evaluate(ctx);
        return new PolicyEvaluationResult(
                result.allowed(),
                result.violationReason()
        );
    }
}

