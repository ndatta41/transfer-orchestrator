package com.company.orchestrator.infrastructure.edc.dto;

public record ContractNegotiationRequest(
        String consumerId,
        String providerId,
        String dataType
) {}
