package com.company.orchestrator.domain.service;

import com.company.orchestrator.api.controller.DemoPolicies;
import com.company.orchestrator.api.dto.TransferAnalyticsResponse;
import com.company.orchestrator.api.dto.TransferRequestDto;
import com.company.orchestrator.api.dto.TransferSummaryResponse;
import com.company.orchestrator.domain.exception.TransferNotFoundException;
import com.company.orchestrator.domain.model.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.company.orchestrator.infrastructure.edc.EdcConnectorClient;
import com.company.orchestrator.infrastructure.edc.dto.ContractNegotiationRequest;
import com.company.orchestrator.infrastructure.edc.dto.ContractNegotiationResult;
import com.company.orchestrator.infrastructure.edc.dto.DataTransferRequest;
import com.company.orchestrator.infrastructure.edc.dto.DataTransferResult;
import com.company.orchestrator.infrastructure.persistence.entity.AuditEventEntity;
import com.company.orchestrator.infrastructure.persistence.entity.TransferEntity;
import com.company.orchestrator.infrastructure.persistence.repository.TransferRepository;
import com.company.orchestrator.policy.Policy;
import com.company.orchestrator.policy.PolicyContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class TransferOrchestrationService
        implements TransferOrchestrator {

    private final TransferRepository repository;
    private final PolicyEvaluationService policyService;
    private final AuditService auditService;
    private final EdcConnectorClient edcConnectorClient;

    public TransferOrchestrationService(
            TransferRepository repository,
            PolicyEvaluationService policyService,
            AuditService auditService,
            EdcConnectorClient edcConnectorClient
    ) {
        this.repository = repository;
        this.policyService = policyService;
        this.auditService = auditService;
        this.edcConnectorClient = edcConnectorClient;
    }

    @Override
    public UUID initiateTransfer(TransferRequestDto dto) {
        Policy policy = DemoPolicies.defaultPolicy();

        PolicyContext context = new PolicyContext(
                dto.consumerId(),
                dto.providerId(),
                dto.dataType(),
                "EU",
                Set.of("ISO_9001"),
                "QUALITY_ANALYSIS",
                Instant.now(),
                ZoneId.of("CET"),
                10
        );
        TransferEntity entity =  new TransferEntity(
                dto.consumerId(),
                dto.providerId(),
                dto.dataType()
        );
        entity = repository.save(entity);
        TransferRequest request = new TransferRequest(
                entity.getId(),
                dto.consumerId(),
                dto.providerId(),
                dto.dataType(),
                policy,
                context
        );
        auditService.logTransferRequest(request);
        entity.setState(TransferState.POLICY_EVALUATION);
        var result = policyService.evaluate(
                request.policy(),
                request.policyContext()
        );
        auditService.logPolicyEvaluation(
                entity.getId(),
                result
        );
        if (!result.allowed()) {
            entity.setState(TransferState.DENIED);
            return entity.getId();
        }
        entity.setState(TransferState.APPROVED);
        // EDC steps handled in infrastructure layer
        auditService.logStateTransition(entity.getId(), entity.getState(), TransferState.CONTRACT_NEGOTIATION);
        ContractNegotiationResult contractNegotiationResult = edcConnectorClient.negotiateContract(new ContractNegotiationRequest(request.consumerId(), request.providerId(), request.dataType()));
        entity.setState(TransferState.NEGOTIATED);
        auditService.logStateTransition(entity.getId(), TransferState.CONTRACT_NEGOTIATION, TransferState.NEGOTIATED);
        auditService.logStateTransition(entity.getId(), entity.getState(), TransferState.TRANSFER_IN_PROGRESS);
        DataTransferResult dataTransferResult = edcConnectorClient.initiateTransfer(
                new DataTransferRequest(
                        contractNegotiationResult.contractAgreementId(),
                        UUID.randomUUID().toString(),
                        UUID.randomUUID().toString())
        );
        entity.setState(TransferState.TRANSFER_IN_PROGRESS);
        if(dataTransferResult.success()){
            auditService.logStateTransition(entity.getId(), entity.getState(), TransferState.COMPLETED);
            entity.setState(TransferState.COMPLETED);
        } else {
            auditService.logStateTransition(entity.getId(), entity.getState(), TransferState.FAILED);
            entity.setState(TransferState.FAILED);
        }
        entity = repository.save(entity);
        return entity.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public TransferStatus getTransferStatus(UUID transferId) {
        var entity = repository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(transferId));

        return new TransferStatus(
                entity.getId(),
                entity.getState(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public void cancelTransfer(UUID transferId) {
        var entity = repository.findById(transferId)
                .orElseThrow(() -> new TransferNotFoundException(transferId));

        entity.setState(TransferState.CANCELLED);
        auditService.logStateTransition(
                transferId,
                entity.getState(),
                TransferState.CANCELLED
        );
    }

    @Override
    public List<AuditEventEntity> getTransferAuditLog(UUID transferId) {
        return auditService.getAuditTrail(transferId);
    }

    @Override
    public Page<TransferSummaryResponse> listTransfers(Pageable pageable) {
        return repository.findAll(pageable)
                .map(entity -> new TransferSummaryResponse(
                        entity.getId(),
                        entity.getConsumerId(),
                        entity.getProviderId(),
                        entity.getDataType(),
                        entity.getState().name(),
                        entity.getCreatedAt()
                ));
    }

    @Override
    public TransferAnalyticsResponse getAnalytics() {

        long total = repository.count();

        Map<String, Long> byState =
                repository.countByState().stream()
                        .collect(Collectors.toMap(
                                row -> row[0].toString(),
                                row -> (Long) row[1]
                        ));

        Map<String, Long> byDataType =
                repository.countByDataType().stream()
                        .collect(Collectors.toMap(
                                row -> row[0].toString(),
                                row -> (Long) row[1]
                        ));

        return new TransferAnalyticsResponse(
                total,
                byState,
                byDataType
        );
    }








}
