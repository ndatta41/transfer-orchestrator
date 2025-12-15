package com.company.orchestrator.infrastructure.edc;

import java.util.UUID;

import com.company.orchestrator.domain.model.TransferState;
import com.company.orchestrator.infrastructure.edc.dto.ContractNegotiationRequest;
import com.company.orchestrator.infrastructure.edc.dto.ContractNegotiationResult;
import com.company.orchestrator.infrastructure.edc.dto.DataTransferRequest;
import com.company.orchestrator.infrastructure.edc.dto.DataTransferResult;
import org.springframework.stereotype.Component;

@Component
public class MockEdcConnectorClient implements EdcConnectorClient {
    @Override
    public ContractNegotiationResult negotiateContract(ContractNegotiationRequest request) {
        return ContractNegotiationResult.ok(UUID.randomUUID().toString());
    }

    @Override
    public DataTransferResult initiateTransfer(DataTransferRequest request) {
        return DataTransferResult.ok(UUID.randomUUID().toString());
    }

    @Override
    public TransferState getTransferState(String transferProcessId) {
        return TransferState.TRANSFER_IN_PROGRESS;
    }

    @Override
    public void terminateTransfer(String transferProcessId) {

    }
}
