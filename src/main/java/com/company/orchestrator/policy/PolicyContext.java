package com.company.orchestrator.policy;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Set;

public record PolicyContext(
        String consumerId,
        String providerId,
        String dataType,
        String consumerRegion,
        Set<String> consumerCertifications,
        String usagePurpose,
        Instant requestTime,
        ZoneId timeZone,
        long requestsInLastHour
) {}
