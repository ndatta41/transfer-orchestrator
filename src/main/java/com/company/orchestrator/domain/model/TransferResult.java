package com.company.orchestrator.domain.model;

public record TransferResult(
        boolean success,
        String message
) {
    public static TransferResult ok() {
        return new TransferResult(true, "Transfer completed successfully");
    }

    public static TransferResult failure(String reason) {
        return new TransferResult(false, reason);
    }
}
