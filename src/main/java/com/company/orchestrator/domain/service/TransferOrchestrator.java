package com.company.orchestrator.domain.service;

import com.company.orchestrator.api.dto.TransferAnalyticsResponse;
import com.company.orchestrator.api.dto.TransferRequestDto;
import com.company.orchestrator.api.dto.TransferSummaryResponse;
import com.company.orchestrator.domain.model.TransferStatus;
import com.company.orchestrator.infrastructure.persistence.entity.AuditEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TransferOrchestrator {

    UUID initiateTransfer(TransferRequestDto request);

    TransferStatus getTransferStatus(UUID transferId);

    void cancelTransfer(UUID transferId);

    List<AuditEventEntity> getTransferAuditLog(UUID transferId);

    Page<TransferSummaryResponse> listTransfers(Pageable pageable);

    TransferAnalyticsResponse getAnalytics();
}
