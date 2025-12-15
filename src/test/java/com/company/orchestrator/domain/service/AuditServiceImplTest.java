package com.company.orchestrator.domain.service;

import com.company.orchestrator.audit.AuditAction;
import com.company.orchestrator.audit.ComplianceReport;
import com.company.orchestrator.domain.model.TransferRequest;
import com.company.orchestrator.domain.model.TransferResult;
import com.company.orchestrator.domain.model.TransferState;
import com.company.orchestrator.infrastructure.persistence.entity.AuditEventEntity;
import com.company.orchestrator.infrastructure.persistence.repository.AuditEventRepository;
import com.company.orchestrator.policy.PolicyEvaluationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock
    private AuditEventRepository repository;

    @InjectMocks
    private AuditServiceImpl auditService;

    private UUID transferId;

    @BeforeEach
    void setUp() {
        transferId = UUID.randomUUID();
    }

    // ---------------------------------------------------------
    // logTransferRequest
    // ---------------------------------------------------------

    @Test
    void logTransferRequest_savesCorrectAuditEvent() {
        TransferRequest request = mock(TransferRequest.class);
        when(request.transferId()).thenReturn(transferId);
        when(request.consumerId()).thenReturn("consumer-123");

        ArgumentCaptor<AuditEventEntity> captor =
                ArgumentCaptor.forClass(AuditEventEntity.class);

        auditService.logTransferRequest(request);

        verify(repository).save(captor.capture());
        AuditEventEntity event = captor.getValue();

        assertThat(event.getTransferId()).isEqualTo(transferId);
        assertThat(event.getAction()).isEqualTo(AuditAction.TRANSFER_REQUESTED);
        assertThat(event.getActor()).isEqualTo("API");
        assertThat(event.getMetadata()).isEqualTo("Consumer=consumer-123");
    }

    @Test
    void logPolicyEvaluation_whenApproved() {
        PolicyEvaluationResult result = mock(PolicyEvaluationResult.class);
        when(result.allowed()).thenReturn(true);

        ArgumentCaptor<AuditEventEntity> captor =
                ArgumentCaptor.forClass(AuditEventEntity.class);

        auditService.logPolicyEvaluation(transferId, result);

        verify(repository).save(captor.capture());
        AuditEventEntity event = captor.getValue();

        assertThat(event.getAction()).isEqualTo(AuditAction.POLICY_EVALUATED);
        assertThat(event.getActor()).isEqualTo("POLICY_ENGINE");
        assertThat(event.getMetadata()).isEqualTo("APPROVED");
    }

    @Test
    void logPolicyEvaluation_whenDenied() {
        PolicyEvaluationResult result = mock(PolicyEvaluationResult.class);
        when(result.allowed()).thenReturn(false);
        when(result.violationReason()).thenReturn("NOT_AUTHORIZED");

        ArgumentCaptor<AuditEventEntity> captor =
                ArgumentCaptor.forClass(AuditEventEntity.class);

        auditService.logPolicyEvaluation(transferId, result);

        verify(repository).save(captor.capture());
        AuditEventEntity event = captor.getValue();

        assertThat(event.getMetadata())
                .isEqualTo("DENIED: NOT_AUTHORIZED");
    }

    @Test
    void logStateTransition_savesCorrectTransition() {
        ArgumentCaptor<AuditEventEntity> captor =
                ArgumentCaptor.forClass(AuditEventEntity.class);

        auditService.logStateTransition(
                transferId,
                TransferState.REQUESTED,
                TransferState.POLICY_EVALUATION
        );

        verify(repository).save(captor.capture());
        AuditEventEntity event = captor.getValue();

        assertThat(event.getAction()).isEqualTo(AuditAction.STATE_TRANSITION);
        assertThat(event.getActor()).isEqualTo("ORCHESTRATOR");
        assertThat(event.getMetadata())
                .isEqualTo("REQUESTED -> POLICY_EVALUATION");
    }

    @Test
    void logTransferCompletion_whenSuccess() {
        TransferResult result = mock(TransferResult.class);
        when(result.success()).thenReturn(true);
        when(result.message()).thenReturn("OK");

        ArgumentCaptor<AuditEventEntity> captor =
                ArgumentCaptor.forClass(AuditEventEntity.class);

        auditService.logTransferCompletion(transferId, result);

        verify(repository).save(captor.capture());
        AuditEventEntity event = captor.getValue();

        assertThat(event.getAction())
                .isEqualTo(AuditAction.TRANSFER_COMPLETED);
        assertThat(event.getMetadata()).isEqualTo("OK");
    }

    @Test
    void logTransferCompletion_whenFailure() {
        TransferResult result = mock(TransferResult.class);
        when(result.success()).thenReturn(false);
        when(result.message()).thenReturn("TIMEOUT");

        ArgumentCaptor<AuditEventEntity> captor =
                ArgumentCaptor.forClass(AuditEventEntity.class);

        auditService.logTransferCompletion(transferId, result);

        verify(repository).save(captor.capture());
        AuditEventEntity event = captor.getValue();

        assertThat(event.getAction())
                .isEqualTo(AuditAction.TRANSFER_FAILED);
        assertThat(event.getMetadata()).isEqualTo("TIMEOUT");
    }

    @Test
    void getAuditTrail_returnsRepositoryResult() {
        List<AuditEventEntity> events =
                List.of(mock(AuditEventEntity.class));

        when(repository.findByTransferIdOrderByTimestampAsc(transferId))
                .thenReturn(events);

        List<AuditEventEntity> result =
                auditService.getAuditTrail(transferId);

        assertThat(result).isSameAs(events);
        verify(repository)
                .findByTransferIdOrderByTimestampAsc(transferId);
    }

    @Test
    void generateComplianceReport_aggregatesCountsCorrectly() {
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        AuditEventEntity completed = mock(AuditEventEntity.class);
        when(completed.getAction())
                .thenReturn(AuditAction.TRANSFER_COMPLETED);

        AuditEventEntity failed = mock(AuditEventEntity.class);
        when(failed.getAction())
                .thenReturn(AuditAction.TRANSFER_FAILED);

        AuditEventEntity requested = mock(AuditEventEntity.class);
        when(requested.getAction())
                .thenReturn(AuditAction.TRANSFER_REQUESTED);

        when(repository.findByTimestampBetween(from, to))
                .thenReturn(List.of(
                        completed,
                        failed,
                        requested,
                        completed
                ));

        ComplianceReport report =
                auditService.generateComplianceReport(from, to);

        assertThat(report.from()).isEqualTo(from);
        assertThat(report.to()).isEqualTo(to);
        assertThat(report.totalTransfers()).isEqualTo(3);
        assertThat(report.successfulTransfers()).isEqualTo(2);
        assertThat(report.failedTransfers()).isEqualTo(1);

        Map<AuditAction, Long> counts = report.actionCounts();
        assertThat(counts.get(AuditAction.TRANSFER_COMPLETED)).isEqualTo(2);
        assertThat(counts.get(AuditAction.TRANSFER_FAILED)).isEqualTo(1);
        assertThat(counts.get(AuditAction.TRANSFER_REQUESTED)).isEqualTo(1);
    }

    @Test
    void generateComplianceReport_whenNoEvents() {
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();

        when(repository.findByTimestampBetween(from, to))
                .thenReturn(List.of());

        ComplianceReport report =
                auditService.generateComplianceReport(from, to);

        assertThat(report.totalTransfers()).isZero();
        assertThat(report.successfulTransfers()).isZero();
        assertThat(report.failedTransfers()).isZero();
        assertThat(report.actionCounts()).isEmpty();
    }
}
