package com.company.orchestrator.audit;

import java.time.Instant;
import java.util.Map;

public record ComplianceReport(
        Instant from,
        Instant to,
        long totalTransfers,
        long successfulTransfers,
        long failedTransfers,
        Map<AuditAction, Long> actionCounts
) {}
