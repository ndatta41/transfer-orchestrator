package com.company.orchestrator.api.dto;

import java.util.Map;

public record TransferAnalyticsResponse(
        long totalTransfers,
        Map<String, Long> byState,
        Map<String, Long> byDataType
) {}