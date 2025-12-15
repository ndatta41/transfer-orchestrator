package com.company.orchestrator.infrastructure.edc.dto;

public record ContractNegotiationResult(
        boolean success,
        String contractAgreementId,
        String errorMessage
) {
    public static ContractNegotiationResult ok(String id) {
        return new ContractNegotiationResult(true, id, null);
    }

    public static ContractNegotiationResult failure(String error) {
        return new ContractNegotiationResult(false, null, error);
    }
}
