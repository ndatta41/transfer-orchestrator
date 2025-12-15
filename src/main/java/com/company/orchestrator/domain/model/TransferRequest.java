package com.company.orchestrator.domain.model;

import com.company.orchestrator.policy.Policy;
import com.company.orchestrator.policy.PolicyContext;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public record TransferRequest(
        UUID transferId,
        String consumerId,
        String providerId,
        String dataType,
        Policy policy,
        PolicyContext policyContext
) {}
