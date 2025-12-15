package com.company.orchestrator.infrastructure.persistence;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (var conn = dataSource.getConnection()) {
            if (conn.isValid(1)) {
                return Health.up().withDetail("database", "OK").build();
            } else {
                return Health.down().withDetail("database", "Connection invalid").build();
            }
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
