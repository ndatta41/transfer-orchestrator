package com.company.orchestrator.infrastructure.edc.dto;

public record DataTransferRequest(
        String contractAgreementId,
        String sourceEndpoint,
        String destinationEndpoint
) {}
