package com.company.orchestrator.api.dto;

import com.company.orchestrator.domain.model.TransferState;
import java.time.Instant;
import java.util.UUID;

public record TransferStatusDto(
        UUID transferId,
        TransferState state,
        Instant lastUpdated
) {}
