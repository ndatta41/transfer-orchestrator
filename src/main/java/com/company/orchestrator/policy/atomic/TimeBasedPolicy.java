package com.company.orchestrator.policy.atomic;

import com.company.orchestrator.policy.*;
import java.time.LocalTime;
import java.time.ZoneId;

public final class TimeBasedPolicy implements AtomicPolicy {

    private final LocalTime start;
    private final LocalTime end;
    private final ZoneId zone;

    public TimeBasedPolicy(LocalTime start, LocalTime end, ZoneId zone) {
        this.start = start;
        this.end = end;
        this.zone = zone;
    }

    @Override
    public PolicyEvaluationResult evaluate(PolicyContext context) {
        var localTime = context.requestTime()
                .atZone(zone)
                .toLocalTime();

        if (localTime.isBefore(start) || localTime.isAfter(end)) {
            return PolicyEvaluationResult.deny(
                    "Transfer not allowed outside business hours"
            );
        }
        return PolicyEvaluationResult.allow();
    }
}
