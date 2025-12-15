package com.company.orchestrator.domain.service;

import com.company.orchestrator.audit.AuditAction;
import com.company.orchestrator.audit.ComplianceReport;
import com.company.orchestrator.domain.model.TransferRequest;
import com.company.orchestrator.domain.model.TransferResult;
import com.company.orchestrator.domain.model.TransferState;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;

import com.company.orchestrator.infrastructure.persistence.entity.AuditEventEntity;
import com.company.orchestrator.infrastructure.persistence.repository.AuditEventRepository;
import com.company.orchestrator.policy.PolicyEvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    private final AuditEventRepository repository;

    public AuditServiceImpl(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public void logTransferRequest(TransferRequest request) {
        save(
                request.transferId(),
                AuditAction.TRANSFER_REQUESTED,
                "API",
                "Consumer=" + request.consumerId()
        );
    }

    @Override
    public void logPolicyEvaluation(
            UUID transferId,
            PolicyEvaluationResult result
    ) {
        save(
                transferId,
                AuditAction.POLICY_EVALUATED,
                "POLICY_ENGINE",
                result.allowed() ? "APPROVED" : "DENIED: " + result.violationReason()
        );
    }

    @Override
    public void logStateTransition(
            UUID transferId,
            TransferState from,
            TransferState to
    ) {
        save(
                transferId,
                AuditAction.STATE_TRANSITION,
                "ORCHESTRATOR",
                from + " -> " + to
        );
    }

    @Override
    public void logTransferCompletion(
            UUID transferId,
            TransferResult result
    ) {
        save(
                transferId,
                result.success()
                        ? AuditAction.TRANSFER_COMPLETED
                        : AuditAction.TRANSFER_FAILED,
                "ORCHESTRATOR",
                result.message()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditEventEntity> getAuditTrail(UUID transferId) {
        return repository.findByTransferIdOrderByTimestampAsc(transferId);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplianceReport generateComplianceReport(
            Instant from,
            Instant to
    ) {
        var events = repository.findByTimestampBetween(from, to);

        var counts = new EnumMap<AuditAction, Long>(AuditAction.class);
        events.forEach(e ->
                counts.merge(e.getAction(), 1L, Long::sum)
        );

        long completed =
                counts.getOrDefault(AuditAction.TRANSFER_COMPLETED, 0L);
        long failed =
                counts.getOrDefault(AuditAction.TRANSFER_FAILED, 0L);

        return new ComplianceReport(
                from,
                to,
                completed + failed,
                completed,
                failed,
                counts
        );
    }

    private void save(
            UUID transferId,
            AuditAction action,
            String actor,
            String metadata
    ) {
    repository.save(new AuditEventEntity(transferId, action, actor, metadata));
    }
}
