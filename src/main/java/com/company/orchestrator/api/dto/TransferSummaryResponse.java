package com.company.orchestrator.api.dto;

import java.time.Instant;
import java.util.UUID;

public record TransferSummaryResponse(
        UUID id,
        String consumerId,
        String providerId,
        String dataType,
        String state,
        Instant createdAt
) {}