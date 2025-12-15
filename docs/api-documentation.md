
# Transfer Orchestrator API Documentation

Base URL: `http://localhost:8080/api/v1/transfers`

---

## 1. Initiate a Transfer

**Endpoint:** `POST /api/v1/transfers`  
**Description:** Create a new transfer request.

**Request Body:**

```json
{
  "consumerId": "consumer1",
  "providerId": "provider1",
  "dataType": "DATA_TYPE"
}
```

**Response:**

```json
{
  "id": "d290f1ee-6c54-4b01-90e6-d701748f0851"
}
```

**Notes:**

- Returns the generated transfer ID.
- Workflow triggers audit events:
    - `TRANSFER_REQUESTED`
    - `POLICY_EVALUATED`
    - `TRANSFER_COMPLETED` (if successful)

---

## 2. Get Transfer Status

**Endpoint:** `GET /api/v1/transfers/{id}`  
**Description:** Retrieve current status of a transfer.

**Path Parameters:**

- `id` – UUID of the transfer

**Response:**

```json
{
  "transferId": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "state": "TRANSFER_COMPLETED",
  "lastUpdated": "2025-12-15T14:30:00Z"
}
```

---

## 3. Cancel Transfer

**Endpoint:** `DELETE /api/v1/transfers/{id}`  
**Description:** Cancel an active transfer.

**Path Parameters:**

- `id` – UUID of the transfer

**Response:**

```http
204 No Content
```

**Notes:**

- The transfer state is updated to `CANCELLED`.
- Audit event `STATE_TRANSITION` is logged.

---

## 4. Get Audit Trail

**Endpoint:** `GET /api/v1/transfers/{id}/audit`  
**Description:** Retrieve the audit log for a specific transfer.

**Path Parameters:**

- `id` – UUID of the transfer

**Response:**

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

## 5. List Transfers (Paginated)

**Endpoint:** `GET /api/v1/transfers`  
**Description:** Retrieve a paginated list of transfers.

**Query Parameters:**

- `page` – Page number (default: 0)
- `size` – Page size (default: 20)
- `sort` – Sort field and direction, e.g., `createdAt,desc`

**Response:**

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

## 6. Transfer Analytics

**Endpoint:** `GET /api/v1/transfers/analytics`  
**Description:** Retrieve aggregated analytics for all transfers.

**Response:**

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

## Notes

- All timestamps are in ISO 8601 format (UTC).
- End-to-end tests cover the workflow: `TRANSFER_REQUESTED`, `POLICY_EVALUATED`, `TRANSFER_COMPLETED`/`FAILED`, and audit log verification.
- API supports UUID-based transfer IDs.
