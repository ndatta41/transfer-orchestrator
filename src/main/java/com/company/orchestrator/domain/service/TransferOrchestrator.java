package com.company.orchestrator.domain.service;

import com.company.orchestrator.domain.model.TransferRequest;
import com.company.orchestrator.domain.model.TransferStatus;
import com.company.orchestrator.infrastructure.persistence.entity.AuditEventEntity;

import java.util.List;
import java.util.UUID;

public interface TransferOrchestrator {

    UUID initiateTransfer(TransferRequest request);

    TransferStatus getTransferStatus(UUID transferId);

    void cancelTransfer(UUID transferId);

    List<AuditEventEntity> getTransferAuditLog(UUID transferId);
}
