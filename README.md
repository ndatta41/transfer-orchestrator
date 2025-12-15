
# Transfer Orchestrator

A Spring Boot-based service for orchestrating and auditing secure data transfers between consumers and providers. This service supports:

- Policy evaluation (atomic and composite policies)
- Transfer state management
- Audit logging for compliance
- Integration with external data transfer systems (EDC)

---

## Features

1. **Transfer Management**
   - Initiate, cancel, and track transfers.
   - Supports multiple states: `TRANSFER_REQUESTED`, `POLICY_EVALUATED`, `APPROVED`, `DENIED`, `TRANSFER_COMPLETED`, `TRANSFER_FAILED`, `CANCELLED`.

2. **Policy Evaluation**
   - Atomic policies: Certification, Geographic, Rate Limit, Time-based, Usage.
   - Composite policies: AND, OR, NOT.
   - Short-circuit evaluation for composite policies.

3. **Audit Logging**
   - All critical transfer actions are logged.
   - Audit logs stored in `audit_events` table with timestamps and metadata.
   - Compliance reports can be generated from audit logs.

4. **Analytics**
   - Aggregated transfer statistics by state and data type.
   - Paginated listing of transfers.

5. **Integration**
   - External EDC client integration for contract negotiation and data transfer.

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL (or use H2 for testing)

### Database Setup

```sql
CREATE TABLE transfers (
    id UUID PRIMARY KEY,
    consumer_id VARCHAR(255) NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    data_type VARCHAR(255) NOT NULL,
    state VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_transfers_state ON transfers(state);
CREATE INDEX idx_transfers_created_at ON transfers(created_at);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY NOT NULL,
    transfer_id UUID NOT NULL REFERENCES transfers(id) ON DELETE CASCADE,
    action VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    actor VARCHAR(255) NOT NULL,
    metadata TEXT
);

CREATE INDEX idx_audit_transfer ON audit_events(transfer_id);
CREATE INDEX idx_audit_timestamp ON audit_events(timestamp);
```

---

### Running the Application

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

- API Base URL: `http://localhost:8080/api/v1/transfers`

---

## API Endpoints

### 1. Initiate a Transfer

**Request**

```http
POST /api/v1/transfers
Content-Type: application/json

{
  "consumerId": "consumer1",
  "providerId": "provider1",
  "dataType": "DATA_TYPE"
}
```

**Response**

```json
{
  "id": "d290f1ee-6c54-4b01-90e6-d701748f0851"
}
```

---

### 2. Get Transfer Status

**Request**

```http
GET /api/v1/transfers/{id}
```

**Response**

```json
{
  "transferId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "state": "TRANSFER_COMPLETED",
  "lastUpdated": "2025-12-15T14:30:00Z"
}
```

---

### 3. Cancel a Transfer

**Request**

```http
DELETE /api/v1/transfers/{id}
```

**Response**

```http
204 No Content
```

- The transfer state is updated to `CANCELLED`.

---

### 4. Get Audit Trail

**Request**

```http
GET /api/v1/transfers/{id}/audit
```

**Response**

```json
[
  {
    "id": "1a2b3c4d-5678-90ab-cdef-1234567890ab",
    "transferId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
    "action": "TRANSFER_REQUESTED",
    "timestamp": "2025-12-15T14:20:00Z",
    "actor": "API",
    "metadata": "Consumer=consumer1"
  },
  {
    "id": "2b3c4d5e-6789-01ab-cdef-2345678901bc",
    "transferId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
    "action": "POLICY_EVALUATED",
    "timestamp": "2025-12-15T14:21:00Z",
    "actor": "POLICY_ENGINE",
    "metadata": "APPROVED"
  }
]
```

---

### 5. List Transfers (Paginated)

**Request**

```http
GET /api/v1/transfers?page=0&size=10&sort=createdAt,desc
```

**Response**

```json
{
  "content": [
    {
      "id": "d290f1ee-6c54-4b01-90e6-d701748f0851",
      "consumerId": "consumer1",
      "providerId": "provider1",
      "dataType": "DATA_TYPE",
      "state": "TRANSFER_COMPLETED",
      "createdAt": "2025-12-15T14:20:00Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

---

### 6. Transfer Analytics

**Request**

```http
GET /api/v1/transfers/analytics
```

**Response**

```json
{
  "totalTransfers": 10,
  "byState": {
    "TRANSFER_COMPLETED": 7,
    "TRANSFER_FAILED": 2,
    "CANCELLED": 1
  },
  "byDataType": {
    "DATA_TYPE": 10
  }
}
```

---

## Testing

- Integration tests use **H2 in-memory database**.
- End-to-end tests cover the full workflow including:
   - `TRANSFER_REQUESTED`
   - `POLICY_EVALUATED`
   - `TRANSFER_COMPLETED` or `FAILED`
   - Audit log verification

```bash
mvn test
```

- Mocked EDC client ensures tests run without external dependencies.

---

## Project Structure

```
src/
 └─ main/java/com/company/orchestrator/
     ├─ api/controller/           # REST controllers
     ├─ domain/model/             # Transfer, state, policy context
     ├─ domain/service/           # Orchestrator and audit services
     ├─ infrastructure/persistence/ # Repositories and entities
     └─ policy/                   # Atomic and composite policies
```

---

## License

This project is open-source under the MIT License.
