# Class Diagram - Returns & Disposal Domain

> **Document ID:** class-006
> **Phiên bản:** 1.0.0
> **Ngày:** 2026-04-25
> **Domain:** Returns & Disposals
> **Entities:** ReturnRequest, ReturnToWarehouseRequest, DisposalRequest, DisposalRequestItem

---

## 1. Class Diagram

```mermaid
classDiagram
    direction TB

    class ReturnRequest {
        +Long id
        +String reason
        +String responseNote
        +ReturnRequestStatus status
        +LocalDateTime createdAt
        +LocalDateTime processedAt
        +Order order
        +User createdBy
        +User processedBy
        ---
        +createRequest()
        +approve()
        +reject()
    }

    class ReturnToWarehouseRequest {
        +Long id
        +Integer quantity
        +String reason
        +String responseNote
        +ReturnToWarehouseRequestStatus status
        +LocalDateTime createdAt
        +LocalDateTime processedAt
        +Book book
        +User createdBy
        +User processedBy
        ---
        +createRequest()
        +approve()
        +reject()
    }

    class DisposalRequest {
        +Long id
        +String reason
        +String responseNote
        +DisposalRequestStatus status
        +LocalDateTime createdAt
        +LocalDateTime processedAt
        +User createdBy
        +User processedBy
        +List~DisposalRequestItem~ items
        ---
        +createRequest()
        +approve()
        +reject()
    }

    class DisposalRequestItem {
        +Long id
        +Integer quantity
        +Integer remainingQuantityAfter
        +DisposalRequest disposalRequest
        +Batch batch
        ---
        +dispose()
    }

    class Order {
        +Long id
        +OrderStatus status
        +User user
        +List~OrderDetail~ orderDetails
        ---
        +createReturnRequest()
    }

    class Batch {
        +Long id
        +Integer remainingQuantity
        +List~DisposalRequestItem~ disposalItems
        ---
        +dispose(quantity)
    }

    %% Relationships
    ReturnRequest "N" o-- "1" Order : requests return for
    ReturnRequest "N" o-- "1" User : created by
    ReturnRequest "N" o-- "0..1" User : processed by

    ReturnToWarehouseRequest "N" o-- "1" Book : book being returned
    ReturnToWarehouseRequest "N" o-- "1" User : created by
    ReturnToWarehouseRequest "N" o-- "0..1" User : processed by

    DisposalRequest "1" *-- "0..*" DisposalRequestItem : contains
    DisposalRequest "N" o-- "1" User : created by (WS)
    DisposalRequest "N" o-- "0..1" User : processed by (Admin)

    DisposalRequestItem "N" o-- "1" Batch : batch reference
    DisposalRequestItem "N" o-- "1" DisposalRequest : belongs to
```

---

## 2. Status Flows

### ReturnRequest Status
```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> APPROVED : Seller Approves
    PENDING --> REJECTED : Seller Rejects
    APPROVED --> [*] : (triggers ReturnToWarehouse)
```

### ReturnToWarehouseRequest Status
```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> APPROVED : WS Approves
    PENDING --> REJECTED : WS Rejects
    APPROVED --> [*] : Stock returned to Batch
```

### DisposalRequest Status
```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> APPROVED : Admin Approves
    PENDING --> REJECTED : Admin Rejects
    APPROVED --> [*] : Batch.remainingQuantity -= qty
```

---

## 3. Return Flow (Customer Return)

```mermaid
flowchart TB
    subgraph "Phase 1: Customer Return"
        U["👤 Customer"] -->|"POST /return-requests"| RR["📋 ReturnRequest"]
        RR -->|"PENDING"| SE["👤 Seller"]
        SE -->|"Approve"| RR_A["✅ APPROVED"]
        SE -->|"Reject"| RR_R["❌ REJECTED"]
    end

    subgraph "Phase 2: Return to Warehouse"
        RR_A -->|"POST /return-to-warehouse-requests"| RWR["📋 ReturnToWarehouse"]
        RWR -->|"PENDING"| WS["👤 Warehouse Staff"]
        WS -->|"Approve"| RWR_A["✅ APPROVED\nBatch += qty"]
        WS -->|"Reject"| RWR_R["❌ REJECTED"]
    end

    subgraph "Phase 3: Order Status"
        RR_A -->|"Order.status → RETURNED"| ORD["📋 Order.RETURNED"]
    end
```

---

## 4. Disposal Flow

```mermaid
flowchart TB
    WS["👤 Warehouse Staff"] -->|"POST /disposal-requests\n(items with batch refs)"| DR["📋 DisposalRequest"]
    DR -->|"PENDING"| AD["👤 Admin"]
    AD -->|"Approve"| DR_A["✅ APPROVED\nBatch.remaining -= qty"]
    AD -->|"Reject"| DR_R["❌ REJECTED"]
```

---

## 5. Entity Details

### ReturnRequest
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, AUTO | Primary key |
| reason | String | 1000 | Reason for return |
| responseNote | String | 1000 | Seller response |
| status | ReturnRequestStatus | NOT NULL | PENDING/APPROVED/REJECTED |

### DisposalRequest
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, AUTO | Primary key |
| reason | String | 1000 | Reason for disposal |
| responseNote | String | 1000 | Admin response |
| status | DisposalRequestStatus | NOT NULL | PENDING/APPROVED/REJECTED |

### DisposalRequestItem
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, AUTO | Primary key |
| quantity | Integer | NOT NULL | Qty to dispose |
| remainingQuantityAfter | Integer | - | Stock after disposal |

---

## 6. API Endpoints

### ReturnRequestController (`/api/return-requests`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | Yes | Create request |
| GET | `/my-requests` | Yes | My requests |
| GET | `/` | Yes | Get all |
| PUT | `/{id}/approve` | Seller | Approve |
| PUT | `/{id}/reject` | Seller | Reject |

### ReturnToWarehouseRequestController (`/api/return-to-warehouse-requests`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | Seller | Create request |
| GET | `/my-requests` | Yes | My requests |
| GET | `/` | Yes | Get all |
| PUT | `/{id}/approve` | WS | Approve |
| PUT | `/{id}/reject` | WS | Reject |

### DisposalRequestController (`/api/disposal-requests`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | WS | Create request |
| GET | `/my-requests` | Yes | My requests |
| GET | `/` | Yes | Get all |
| GET | `/{id}` | Yes | Get by ID |
| PUT | `/{id}/approve` | Admin | Approve |
| PUT | `/{id}/reject` | Admin | Reject |

---

## 7. Business Rules

| Rule | Description |
|------|-------------|
| BR-001 | DisposalRequestItem link đến Batch để track số lượng thanh lý |
| BR-002 | Sau khi approve disposal: `Batch.remainingQuantity -= item.quantity` |
| BR-003 | ReturnToWarehouse tăng `Batch.remainingQuantity` khi approve |
| BR-004 | Approval/Response có audit: processedAt, processedBy |

---

## 8. Related Documents

- **ER Diagram:** `er-diagram/er-001-full.md`
- **Use Case:** `usecase/uc-007.md`
- **Sequence:** `sequence/seq-007.md`, `sequence/seq-008.md`

---

*Generated by Senior BA Agent | BookStore Backend | 2026-04-25*
