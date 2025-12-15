package com.company.orchestrator.infrastructure.edc;

import com.company.orchestrator.domain.model.TransferState;
import com.company.orchestrator.infrastructure.edc.dto.ContractNegotiationRequest;
import com.company.orchestrator.infrastructure.edc.dto.ContractNegotiationResult;
import com.company.orchestrator.infrastructure.edc.dto.DataTransferRequest;
import com.company.orchestrator.infrastructure.edc.dto.DataTransferResult;

import java.util.UUID;

public interface EdcConnectorClient {

    ContractNegotiationResult negotiateContract(ContractNegotiationRequest request);

    DataTransferResult initiateTransfer(DataTransferRequest request);

    TransferState getTransferState(String transferProcessId);

    void terminateTransfer(String transferProcessId);
}
