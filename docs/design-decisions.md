# Transfer Orchestrator - Design Decisions

## 1. Architecture Overview

The Transfer Orchestrator service is built using **Spring Boot** and follows a **modular layered architecture**:

1. **API Layer**: REST controllers exposing endpoints for initiating, tracking, cancelling transfers, and retrieving audit logs and analytics.
2. **Domain Layer**: Core business logic including transfer orchestration, state management, and policy evaluation.
3. **Policy Layer**: Implements atomic and composite policies for secure transfer approval.
4. **Infrastructure Layer**: Persistence (JPA repositories), external system integration (EDC connector), and audit event storage.
5. **Audit Layer**: Logs critical events for compliance reporting.

---

## 2. Transfer Workflow Design

- Transfers move through a **finite set of states**:  
  `TRANSFER_REQUESTED → POLICY_EVALUATED → APPROVED/DENIED → CONTRACT_NEGOTIATION → NEGOTIATED → TRANSFER_IN_PROGRESS → COMPLETED/FAILED`
- **State transitions** are auditable, ensuring full traceability.
- **Policy evaluation** occurs before contract negotiation to enforce compliance early.

---

## 3. Policy Evaluation Design

- **Atomic Policies**: Independent checks such as Certification, Geographic, Rate Limit, Time-based, Usage.
- **Composite Policies**: Logical composition using AND, OR, NOT.
- **Short-circuit Evaluation**: Composite policies stop evaluation as soon as a decision is determined.
- **Extensibility**: New policies can be added by implementing `AtomicPolicy` or `CompositePolicy` interfaces.

---

## 4. Audit and Compliance

- **Audit Events** are recorded for all critical transfer actions:
    - `TRANSFER_REQUESTED`, `POLICY_EVALUATED`, `STATE_TRANSITION`, `TRANSFER_COMPLETED`, `TRANSFER_FAILED`
- **Compliance Reports** can be generated for a given time window.
- Stored in a dedicated `audit_events` table with timestamps, actor, and metadata.
- **EnumMap** is used for fast aggregation by `AuditAction`.

---

## 5. Integration with EDC

- Contract negotiation and data transfer are delegated to an external **EDC Connector** client.
- Integration points are abstracted through DTOs (`ContractNegotiationRequest`, `ContractNegotiationResult`, `DataTransferRequest`, `DataTransferResult`).
- Allows mocking during integration tests to avoid external dependencies.

---

## 6. Database Design

- **Transfers Table**:
    - Stores transfer metadata and state.
    - Indexed by `state` and `created_at` for efficient queries.
- **Audit Events Table**:
    - Stores detailed logs for each transfer.
    - Indexed by `transfer_id` and `timestamp` for fast retrieval.

---

## 7. Error Handling and Exceptions

- **Custom Exception**: `TransferNotFoundException` for missing transfers.
- **Fail-fast Policy Evaluation**: Transfers denied by policy are immediately marked `DENIED`.
- **Audit consistency**: All state transitions are logged even on failure.

---

## 8. Testing and Validation

- **Unit Tests**:
    - Cover policy evaluation, audit service, and orchestration logic.
- **Integration Tests**:
    - Use **H2 in-memory database**.
    - End-to-end coverage including transfer lifecycle, audit logs, and analytics.
- **Mocked External Systems**:
    - EDC client is mocked to simulate contract negotiation and transfer results.

---

## 9. Design Trade-offs

1. **Synchronous Workflow**:
    - Pros: Simple state management, easier audit logging.
    - Cons: Longer request processing time during EDC operations.
2. **Policy Engine Coupling**:
    - Policies are evaluated inside orchestration service.
    - Pros: Immediate decision and audit logging.
    - Cons: Adding complex dynamic policies may require refactoring.
3. **Database Choice**:
    - Chose relational DB (PostgreSQL) for strong consistency and audit log queries.
    - Could consider NoSQL for extremely high-volume transfers in future.

---

## 10. Extensibility Considerations

- **New Transfer States**: Can be added in `TransferState` enum and handled in orchestration workflow.
- **New Policies**: Implement `AtomicPolicy` or `CompositePolicy` and inject via orchestration.
- **Analytics**: Repository methods can be extended for additional aggregations (e.g., by region, purpose).
- **External Systems**: EDC client abstraction allows integration with multiple transfer providers.

---

## 11. Logging and Observability

- **SLF4J** logging in all services.
- Audit logs provide detailed trace for compliance.
- Future improvements: integrate with **Prometheus/Grafana** for monitoring workflow metrics.

---

## 12. Security Considerations

- Transfers are validated against policies before external calls.
- Sensitive metadata is logged carefully (no personal data exposure).
- Future enhancement: integrate OAuth/JWT for API authentication.

---

## 13. Conclusion

The design focuses on:

- **Security**: Through strict policy enforcement.
- **Auditability**: All critical events logged.
- **Extensibility**: Easily add new policies, states, and integrations.
- **Testability**: Unit and integration tests ensure workflow correctness.

