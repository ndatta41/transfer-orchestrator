package com.company.orchestrator.infrastructure.edc.dto;

public record DataTransferResult(
        boolean success,
        String transferProcessId,
        String errorMessage
) {
    public static DataTransferResult ok(String id) {
        return new DataTransferResult(true, id, null);
    }

    public static DataTransferResult failure(String error) {
        return new DataTransferResult(false, null, error);
    }
}
