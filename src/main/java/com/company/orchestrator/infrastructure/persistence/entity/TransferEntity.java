package com.company.orchestrator.infrastructure.persistence.entity;

import com.company.orchestrator.domain.model.TransferState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "transfers")
public class TransferEntity {

    @Id
    private UUID id;

    private String consumerId;
    private String providerId;
    private String dataType;

    @Enumerated(EnumType.STRING)
    private TransferState state;

    private Instant createdAt;
    private Instant updatedAt;

    public TransferEntity() {}

    public TransferEntity(String consumerId, String providerId, String dataType) {
        this.id = UUID.randomUUID();
        this.consumerId = consumerId;
        this.providerId = providerId;
        this.dataType = dataType;
        this.state = TransferState.REQUESTED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}

