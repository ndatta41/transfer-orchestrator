package com.company.orchestrator.infrastructure.persistence.repository;


import java.util.List;
import java.util.UUID;

import com.company.orchestrator.infrastructure.persistence.entity.TransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferRepository extends JpaRepository<TransferEntity, UUID> {

    @Query("""
        SELECT t.state, COUNT(t)
        FROM TransferEntity t
        GROUP BY t.state
    """)
    List<Object[]> countByState();

    @Query("""
        SELECT t.dataType, COUNT(t)
        FROM TransferEntity t
        GROUP BY t.dataType
    """)
    List<Object[]> countByDataType();

}
