package com.company.orchestrator.infrastructure.persistence.repository;


import com.company.orchestrator.domain.model.TransferState;
import java.util.List;
import java.util.UUID;

import com.company.orchestrator.infrastructure.persistence.entity.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<TransferEntity, UUID> {

    // Find all transfers by consumer
    List<TransferEntity> findByConsumerId(String consumerId);

    // Find all transfers by provider
    List<TransferEntity> findByProviderId(String providerId);

    // Find all transfers by state
    List<TransferEntity> findByState(TransferState state);

    // Find all transfers created after a certain timestamp
    List<TransferEntity> findByCreatedAtAfter(java.time.Instant timestamp);
}
