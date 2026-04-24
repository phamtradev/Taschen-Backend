# Class Diagram - Inventory Domain

> **Document ID:** class-004
> **Phiên bản:** 1.0.0
> **Ngày:** 2026-04-25
> **Domain:** Inventory & Warehouse
> **Entities:** Batch, BatchDetail, ImportStock, ImportStockDetail

---

## 1. Class Diagram

```mermaid
classDiagram
    direction TB

    class Batch {
        +Long id
        +String batchCode
        +Integer quantity
        +Integer remainingQuantity
        +Double importPrice
        +LocalDate productionDate
        +LocalDateTime createdAt
        +Supplier supplier
        +User createdBy
        +Book book
        +Variant variant
        +ImportStockDetail importStockDetail
        +List~BatchDetail~ batchDetails
        ---
        +deductStock()
        +restoreStock()
        +isAvailable()
    }

    class BatchDetail {
        +Long id
        +Integer quantity
        +Batch batch
        +OrderDetail orderDetail
        ---
        +allocate()
    }

    class ImportStock {
        +Long id
        +LocalDateTime importDate
        +Boolean received
        +User createdBy
        +Supplier supplier
        +PurchaseOrder purchaseOrder
        +List~ImportStockDetail~ importStockDetails
        ---
        +receive()
        +isPending()
    }

    class ImportStockDetail {
        +Long id
        +Integer quantity
        +Double importPrice
        +ImportStock importStock
        +Book book
        +Variant variant
        +Supplier supplier
        +List~Batch~ batches
        ---
        +createBatches()
    }

    class PurchaseOrder {
        +Long id
        +LocalDateTime createdAt
        +LocalDateTime approvedAt
        +String note
        +String cancelReason
        +PurchaseOrderStatus status
        +Supplier supplier
        +User createdBy
        +User approvedBy
        +List~PurchaseOrderItem~ purchaseOrderItems
        ---
        +approve()
        +reject()
        +cancel()
    }

    class PurchaseOrderItem {
        +Long id
        +Integer quantity
        +Double importPrice
        +Book book
        +Variant variant
        +PurchaseOrder purchaseOrder
        ---
        +getTotalPrice()
    }

    class StockRequest {
        +Long id
        +Integer quantity
        +String reason
        +StockRequestStatus status
        +LocalDateTime createdAt
        +LocalDateTime processedAt
        +String responseMessage
        +Book book
        +Variant variant
        +User createdBy
        +User processedBy
    }

    %% Relationships
    ImportStock "1" *-- "0..*" ImportStockDetail : contains
    ImportStockDetail "1" *-- "0..*" Batch : creates
    ImportStockDetail "N" o-- "1" Supplier : from
    ImportStockDetail "N" o-- "1" Book : contains
    ImportStockDetail "N" o-- "1" Variant : format

    Batch "1" *-- "0..*" BatchDetail : tracks usage
    BatchDetail "N" o-- "1" OrderDetail : fulfilled by

    Batch "N" o-- "1" Supplier : from
    Batch "N" o-- "1" Book : contains
    Batch "N" o-- "1" Variant : format
    Batch "N" o-- "1" User : created by
    Batch "N" o-- "0..1" ImportStockDetail : linked to

    PurchaseOrder "1" *-- "0..*" PurchaseOrderItem : contains
    PurchaseOrderItem "N" o-- "1" Book : contains
    PurchaseOrderItem "N" o-- "1" Variant : format
    PurchaseOrder "1" *-- "0..1" ImportStock : sourced from
    PurchaseOrder "N" o-- "1" Supplier : from
```

---

## 2. Batch Lifecycle

```mermaid
stateDiagram-v2
    [*] --> CREATED
    CREATED --> ACTIVE : Imported to warehouse
    ACTIVE --> DEDUCTING : Order allocated
    DEDUCTING --> ACTIVE : Deducted (FIFO)
    ACTIVE --> EXHAUSTED : remainingQuantity = 0
    EXHAUSTED --> [*]
```

---

## 3. FIFO Stock Allocation

```mermaid
flowchart LR
    subgraph "Batches (sorted by createdAt)"
        B1["Batch #1\nqty=100\nremaining=60\noldest"]
        B2["Batch #2\nqty=50\nremaining=50"]
        B3["Batch #3\nqty=80\nremaining=80\nnewest"]
    end

    subgraph "Order: 30 units"
        A["Allocate from oldest first"]
    end

    B1 --> A
    A -->|"deduct 30"| B1_R["Batch #1\nremaining=30"]
    A -->|"if >100, continue"| B2
```

---

## 4. Entity Details

### Batch
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, AUTO | Primary key |
| batchCode | String | UNIQUE, NOT NULL | Batch identifier |
| quantity | Integer | NOT NULL | Original quantity |
| remainingQuantity | Integer | NOT NULL | Available quantity |
| importPrice | Double | NOT NULL | Cost price |
| productionDate | LocalDate | - | Production date |
| createdAt | LocalDateTime | NOT NULL | Import timestamp |

### ImportStock
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, AUTO | Primary key |
| importDate | LocalDateTime | NOT NULL | Import date |
| received | Boolean | NOT NULL | Received flag |

### StockRequest
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, AUTO | Primary key |
| quantity | Integer | NOT NULL | Requested quantity |
| reason | String | 1000 | Reason for request |
| status | StockRequestStatus | NOT NULL | PENDING/APPROVED/REJECTED/ORDERED |

### PurchaseOrder
| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| id | Long | PK, AUTO | Primary key |
| createdAt | LocalDateTime | NOT NULL | Created timestamp |
| approvedAt | LocalDateTime | - | Approved timestamp |
| note | String | 1000 | Notes |
| cancelReason | String | 1000 | Cancel reason |
| status | PurchaseOrderStatus | NOT NULL | Status |

---

## 5. API Endpoints

### BatchController (`/api/batches`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | Yes | Create batch |
| GET | `/{batchId}` | No | Get by ID |
| GET | `/` | No | Get all |
| GET | `/book/{bookId}` | No | By book |
| GET | `/book/{bookId}/available` | No | Available (FIFO) |
| GET | `/book/{bookId}/total-stock` | No | Total stock |
| PUT | `/book/{bookId}/sync-stock` | Yes | Sync stock |
| POST | `/details` | Yes | Create batch detail |

### ImportStockController (`/api/import-stocks`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | Yes | Create import |
| GET | `/` | Yes | Get all |
| GET | `/{id}` | Yes | Get by ID |
| GET | `/book/{bookId}` | Yes | Import history |
| POST | `/{id}/receive` | Yes | Receive stock |

### StockRequestController (`/api/stock-requests`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | Seller | Create request |
| GET | `/my-requests` | Yes | My requests |
| GET | `/` | Yes | Get all |
| PUT | `/{id}/approve` | Yes | Approve |
| PUT | `/{id}/reject` | Yes | Reject |

### PurchaseOrderController (`/api/purchase-orders`)
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/` | Yes | Create PO |
| POST | `/from-stock-request` | Yes | Create from SR |
| GET | `/` | Yes | Get all |
| GET | `/{id}` | Yes | Get by ID |
| PUT | `/{id}/approve` | Yes | Approve |
| PUT | `/{id}/reject` | Yes | Reject |
| PUT | `/{id}/cancel` | Yes | Cancel |
| POST | `/{id}/pay` | Yes | Pay |

---

## 6. Business Rules

| Rule | Description |
|------|-------------|
| BR-001 | FIFO: Batches sorted by createdAt (oldest first) |
| BR-002 | Batch.remainingQuantity = Batch.quantity - sum(BatchDetail.quantity) |
| BR-003 | ImportStock.receive() tạo Batch cho mỗi ImportStockDetail |
| BR-004 | BatchService.syncStockToBook() = sum(Batch.remainingQuantity) |
| BR-005 | StockRequest → PurchaseOrder: Nhiều SR → 1 PO |
| BR-006 | PurchaseOrder → ImportStock: 1 PO → nhiều ImportStock |

---

## 7. Related Documents

- **ER Diagram:** `er-diagram/er-001-full.md`
- **Use Case:** `usecase/uc-006.md`, `usecase/uc-010.md`
- **Sequence:** `sequence/seq-006.md`, `sequence/seq-010.md`

---

*Generated by Senior BA Agent | BookStore Backend | 2026-04-25*
