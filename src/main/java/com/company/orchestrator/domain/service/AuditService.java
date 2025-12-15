package com.company.orchestrator.domain.service;

import com.company.orchestrator.audit.ComplianceReport;
import com.company.orchestrator.domain.model.TransferRequest;
import com.company.orchestrator.domain.model.TransferResult;
import com.company.orchestrator.domain.model.TransferState;
import com.company.orchestrator.infrastructure.persistence.entity.AuditEventEntity;
import com.company.orchestrator.policy.PolicyEvaluationResult;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AuditService {

    void logTransferRequest(TransferRequest request);

    void logPolicyEvaluation(
            UUID transferId,
            PolicyEvaluationResult result
    );

    void logStateTransition(
            UUID transferId,
            TransferState from,
            TransferState to
    );

    void logTransferCompletion(
            UUID transferId,
            TransferResult result
    );

    List<AuditEventEntity> getAuditTrail(UUID transferId);

    ComplianceReport generateComplianceReport(
            Instant from,
            Instant to
    );
}
