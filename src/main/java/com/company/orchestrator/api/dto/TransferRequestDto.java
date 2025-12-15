package com.company.orchestrator.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TransferRequestDto(
        @NotBlank String consumerId,
        @NotBlank String providerId,
        @NotBlank String dataType
) {}
