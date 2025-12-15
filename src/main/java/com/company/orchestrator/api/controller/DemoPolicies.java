package com.company.orchestrator.api.controller;

import com.company.orchestrator.policy.*;
import com.company.orchestrator.policy.atomic.*;
import com.company.orchestrator.policy.composite.AndPolicy;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

public final class DemoPolicies {

    private DemoPolicies() {}

    public static Policy defaultPolicy() {
        return new AndPolicy(List.of(
                new TimeBasedPolicy(
                        LocalTime.of(8, 0),
                        LocalTime.of(18, 0),
                        ZoneId.of("CET")
                ),
                new GeographicPolicy(),
                new CertificationPolicy("ISO_9001"),
                new RateLimitPolicy(100),
                new UsagePolicy("QUALITY_ANALYSIS")
        ));
    }
}
