package com.company.orchestrator.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "audit_log")
public class AuditLogEntity {

    @Id
    private UUID id;

    private UUID transferId;
    private String action;
    private Instant timestamp;
    private String actor;
    private String metadata;

    public AuditLogEntity(UUID transferId, String action, String actor, String metadata) {
        this.id = UUID.randomUUID();
        this.transferId = transferId;
        this.action = action;
        this.timestamp = Instant.now();
        this.actor = actor;
        this.metadata = metadata;
    }
}

