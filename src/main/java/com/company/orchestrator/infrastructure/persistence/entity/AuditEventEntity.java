package com.company.orchestrator.infrastructure.persistence.entity;

import com.company.orchestrator.audit.AuditAction;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "audit_events",
        indexes = {
                @Index(name = "idx_audit_transfer", columnList = "transferId"),
                @Index(name = "idx_audit_timestamp", columnList = "timestamp")
        })
public class AuditEventEntity {

    @Id
    private UUID id;

    @Column(nullable = false, updatable = false)
    private UUID transferId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private AuditAction action;

    @Column(nullable = false, updatable = false)
    private Instant timestamp;

    @Column(nullable = false, updatable = false)
    private String actor;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String metadata;

    protected AuditEventEntity() {
        // JPA
    }

    public AuditEventEntity(
            UUID transferId,
            AuditAction action,
            String actor,
            String metadata
    ) {
        this.id = UUID.randomUUID();
        this.transferId = transferId;
        this.action = action;
        this.actor = actor;
        this.metadata = metadata;
        this.timestamp = Instant.now();
    }
}
