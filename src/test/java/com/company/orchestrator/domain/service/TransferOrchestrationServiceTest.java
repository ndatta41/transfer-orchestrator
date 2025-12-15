package com.company.orchestrator.domain.service;

import com.company.orchestrator.api.dto.TransferAnalyticsResponse;
import com.company.orchestrator.api.dto.TransferRequestDto;
import com.company.orchestrator.api.dto.TransferSummaryResponse;
import com.company.orchestrator.domain.exception.TransferNotFoundException;
import com.company.orchestrator.domain.model.*;
import com.company.orchestrator.infrastructure.edc.EdcConnectorClient;
import com.company.orchestrator.infrastructure.edc.dto.ContractNegotiationRequest;
import com.company.orchestrator.infrastructure.edc.dto.ContractNegotiationResult;
import com.company.orchestrator.infrastructure.edc.dto.DataTransferRequest;
import com.company.orchestrator.infrastructure.edc.dto.DataTransferResult;
import com.company.orchestrator.infrastructure.persistence.entity.AuditEventEntity;
import com.company.orchestrator.infrastructure.persistence.entity.TransferEntity;
import com.company.orchestrator.infrastructure.persistence.repository.TransferRepository;
import com.company.orchestrator.policy.PolicyEvaluationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferOrchestrationServiceTest {

    @Mock
    private TransferRepository repository;

    @Mock
    private PolicyEvaluationService policyService;

    @Mock
    private AuditService auditService;

    @Mock
    private EdcConnectorClient edcConnectorClient;

    @InjectMocks
    private TransferOrchestrationService service;

    private UUID transferId;

    @BeforeEach
    void setUp() {
        transferId = UUID.randomUUID();
    }

    @Test
    void initiateTransfer_policyDenied_stopsEarly() {
        TransferRequestDto dto = new TransferRequestDto(
                "consumer",
                "provider",
                "DATA_TYPE"
        );

        TransferEntity entity = new TransferEntity(
                dto.consumerId(),
                dto.providerId(),
                dto.dataType()
        );
        entity.setId(transferId);

        when(repository.save(any(TransferEntity.class)))
                .thenReturn(entity);

        PolicyEvaluationResult denied = mock(PolicyEvaluationResult.class);
        when(denied.allowed()).thenReturn(false);

        when(policyService.evaluate(any(), any()))
                .thenReturn(denied);

        UUID result = service.initiateTransfer(dto);

        assertThat(result).isEqualTo(transferId);

        verify(auditService).logTransferRequest(any());
        verify(auditService).logPolicyEvaluation(eq(transferId), eq(denied));
        verify(edcConnectorClient, never()).negotiateContract(any());
        verify(repository, times(1)).save(any(TransferEntity.class));
    }

    @Test
    void initiateTransfer_successfulFlow_completesTransfer() {
        TransferRequestDto dto = new TransferRequestDto(
                "consumer",
                "provider",
                "DATA_TYPE"
        );

        TransferEntity entity = new TransferEntity(
                dto.consumerId(),
                dto.providerId(),
                dto.dataType()
        );
        entity.setId(transferId);

        when(repository.save(any(TransferEntity.class)))
                .thenReturn(entity);

        PolicyEvaluationResult approved = mock(PolicyEvaluationResult.class);
        when(approved.allowed()).thenReturn(true);

        when(policyService.evaluate(any(), any()))
                .thenReturn(approved);

        ContractNegotiationResult contractResult =
                new ContractNegotiationResult(true, "contract-id", null);

        when(edcConnectorClient.negotiateContract(any(ContractNegotiationRequest.class)))
                .thenReturn(contractResult);

        DataTransferResult transferResult = mock(DataTransferResult.class);
        when(transferResult.success()).thenReturn(true);

        when(edcConnectorClient.initiateTransfer(any(DataTransferRequest.class)))
                .thenReturn(transferResult);

        UUID result = service.initiateTransfer(dto);

        assertThat(result).isEqualTo(transferId);

        verify(auditService).logTransferRequest(any());
        verify(auditService).logPolicyEvaluation(eq(transferId), eq(approved));
        verify(auditService, atLeastOnce())
                .logStateTransition(eq(transferId), any(), any());
        verify(edcConnectorClient).negotiateContract(any());
        verify(edcConnectorClient).initiateTransfer(any());
        verify(repository, atLeast(2)).save(any(TransferEntity.class));
    }

    @Test
    void initiateTransfer_dataTransferFails_setsFailedState() {
        TransferRequestDto dto = new TransferRequestDto(
                "consumer",
                "provider",
                "DATA_TYPE"
        );

        TransferEntity entity = new TransferEntity(
                dto.consumerId(),
                dto.providerId(),
                dto.dataType()
        );
        entity.setId(transferId);

        when(repository.save(any(TransferEntity.class)))
                .thenReturn(entity);

        PolicyEvaluationResult approved = mock(PolicyEvaluationResult.class);
        when(approved.allowed()).thenReturn(true);

        when(policyService.evaluate(any(), any()))
                .thenReturn(approved);

        when(edcConnectorClient.negotiateContract(any()))
                .thenReturn(new ContractNegotiationResult(true, "contract", null));

        DataTransferResult failed = mock(DataTransferResult.class);
        when(failed.success()).thenReturn(false);

        when(edcConnectorClient.initiateTransfer(any()))
                .thenReturn(failed);

        service.initiateTransfer(dto);

        assertThat(entity.getState()).isEqualTo(TransferState.FAILED);
    }

    @Test
    void getTransferStatus_returnsStatus() {
        TransferEntity entity = mock(TransferEntity.class);
        Instant updated = Instant.now();

        when(entity.getId()).thenReturn(transferId);
        when(entity.getState()).thenReturn(TransferState.COMPLETED);
        when(entity.getUpdatedAt()).thenReturn(updated);

        when(repository.findById(transferId))
                .thenReturn(Optional.of(entity));

        TransferStatus status = service.getTransferStatus(transferId);

        assertThat(status.transferId()).isEqualTo(transferId);
        assertThat(status.state()).isEqualTo(TransferState.COMPLETED);
        assertThat(status.lastUpdated()).isEqualTo(updated);
    }

    @Test
    void getTransferStatus_whenMissing_throwsException() {
        when(repository.findById(transferId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTransferStatus(transferId))
                .isInstanceOf(TransferNotFoundException.class);
    }

    @Test
    void cancelTransfer_setsCancelledState() {
        TransferEntity entity = mock(TransferEntity.class);

        when(repository.findById(transferId))
                .thenReturn(Optional.of(entity));

        service.cancelTransfer(transferId);

        verify(entity).setState(TransferState.CANCELLED);
        verify(auditService).logStateTransition(
                eq(transferId),
                any(),
                eq(TransferState.CANCELLED)
        );
    }

    @Test
    void getTransferAuditLog_delegatesToAuditService() {
        List<AuditEventEntity> events = List.of(mock(AuditEventEntity.class));

        when(auditService.getAuditTrail(transferId))
                .thenReturn(events);

        List<AuditEventEntity> result =
                service.getTransferAuditLog(transferId);

        assertThat(result).isSameAs(events);
    }

    @Test
    void listTransfers_mapsEntitiesToSummary() {
        TransferEntity entity = mock(TransferEntity.class);

        when(entity.getId()).thenReturn(transferId);
        when(entity.getConsumerId()).thenReturn("consumer");
        when(entity.getProviderId()).thenReturn("provider");
        when(entity.getDataType()).thenReturn("DATA");
        when(entity.getState()).thenReturn(TransferState.APPROVED);
        when(entity.getCreatedAt()).thenReturn(Instant.EPOCH);

        Page<TransferEntity> page =
                new PageImpl<>(List.of(entity));

        when(repository.findAll(any(Pageable.class)))
                .thenReturn(page);

        Page<TransferSummaryResponse> result =
                service.listTransfers(PageRequest.of(0, 10));

        TransferSummaryResponse summary = result.getContent().get(0);

        assertThat(summary.id()).isEqualTo(transferId);
        assertThat(summary.state()).isEqualTo("APPROVED");
        assertThat(summary.consumerId()).isEqualTo("consumer");
    }

    @Test
    void getAnalytics_aggregatesCounts() {
        when(repository.count()).thenReturn(5L);

        when(repository.countByState()).thenReturn(List.of(
                new Object[]{TransferState.COMPLETED, 3L},
                new Object[]{TransferState.FAILED, 2L}
        ));

        when(repository.countByDataType()).thenReturn(List.of(
                new Object[]{"TYPE_A", 4L},
                new Object[]{"TYPE_B", 1L}
        ));

        TransferAnalyticsResponse response =
                service.getAnalytics();

        assertThat(response.totalTransfers()).isEqualTo(5L);
        assertThat(response.byState())
                .containsEntry("COMPLETED", 3L)
                .containsEntry("FAILED", 2L);
        assertThat(response.byDataType())
                .containsEntry("TYPE_A", 4L)
                .containsEntry("TYPE_B", 1L);
    }
}
