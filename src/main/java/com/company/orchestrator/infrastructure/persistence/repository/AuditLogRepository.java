package com.company.orchestrator.infrastructure.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.company.orchestrator.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, UUID> {

    // Find all audit logs for a specific transfer
    List<AuditLogEntity> findByTransferId(UUID transferId);

    // Find all logs by action string
    List<AuditLogEntity> findByAction(String action);

    // Find logs after a certain timestamp
    List<AuditLogEntity> findByTimestampAfter(java.time.Instant timestamp);
}

