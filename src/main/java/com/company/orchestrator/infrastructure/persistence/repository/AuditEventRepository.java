package com.company.orchestrator.infrastructure.persistence.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.company.orchestrator.infrastructure.persistence.entity.AuditEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository
        extends JpaRepository<AuditEventEntity, UUID> {

    List<AuditEventEntity> findByTransferIdOrderByTimestampAsc(UUID transferId);

    List<AuditEventEntity> findByTimestampBetween(
            Instant from,
            Instant to
    );
}
