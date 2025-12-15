package com.company.orchestrator.audit;

public enum AuditAction {
    TRANSFER_REQUESTED,
    POLICY_EVALUATED,
    STATE_TRANSITION,
    TRANSFER_COMPLETED,
    TRANSFER_FAILED,
    TRANSFER_CANCELLED
}
