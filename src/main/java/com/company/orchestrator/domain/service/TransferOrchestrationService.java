package com.company.orchestrator.domain.service;

import com.company.orchestrator.domain.exception.TransferNotFoundException;
import com.company.orchestrator.domain.model.*;

import java.util.List;
import java.util.UUID;

import com.company.orchestrator.infrastructure.edc.EdcConnectorClient;
import com.company.orchestrator.infrastructure.edc.dto.ContractNegotiationRequest;
import com.company.orchestrator.infrastructure.edc.dto.ContractNegotiationResult;
import com.company.orchestrator.infrastructure.edc.dto.DataTransferRequest;
import com.company.orchestrator.infrastructure.edc.dto.DataTransferResult;
import com.company.orchestrator.infrastructure.persistence.entity.AuditEventEntity;
import com.company.orchestrator.infrastructure.persistence.entity.TransferEntity;
import com.company.orchestrator.infrastructure.persistence.repository.TransferRepository;
import lombok.extern.slf4j.Slf4j;
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
    public UUID initiateTransfer(TransferRequest request) {
        TransferEntity entity =  new TransferEntity(
                request.consumerId(),
                request.providerId(),
                request.dataType()
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
        entity = repository.save(entity);
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
    public List<AuditEventEntity>
    getTransferAuditLog(UUID transferId) {
        return auditService.getAuditTrail(transferId);
    }
}
