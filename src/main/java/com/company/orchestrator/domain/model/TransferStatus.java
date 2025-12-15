package com.company.orchestrator.domain.model;

import java.time.Instant;
import java.util.UUID;

public record TransferStatus(
        UUID transferId,
        TransferState state,
        Instant lastUpdated
) {}
