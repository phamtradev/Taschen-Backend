# Overall Class Diagram

> **Document ID:** class-000
> **Phiên bản:** 1.1.0
> **Ngày:** 2026-04-25
> **Phạm vi:** Toàn hệ thống — 33 entities, 6 domains

---

## Complete Class Diagram

```mermaid
classDiagram
    direction TB
    namespace Core {
        class User {
            id: Long
            firstName: String
            lastName: String
            gender: Gender
            email: String
            phoneNumber: String
            isActive: Boolean
            password: String
        }
        class Role {
            id: Long
            code: String
            name: String
        }
        class Permission {
            id: Long
            code: String
            httpMethod: HttpMethod
            pathPattern: String
            active: Boolean
        }
        class Address {
            id: Long
            addressType: AddressType
            street: String
            district: String
            ward: String
            city: String
            recipientName: String
            phoneNumber: String
            isDefault: Boolean
        }
        class RefreshToken {
            id: Long
            token: String
            expiresAt: Instant
            revoked: Boolean
            createdAt: Instant
        }
        class VerificationToken {
            id: Long
            token: String
            expiresAt: Instant
        }
    }

    namespace Catalog {
        class Book {
            id: Long
            title: String
            author: String
            description: String
            publicationYear: Integer
            weightGrams: Integer
            pageCount: Integer
            price: Double
            stockQuantity: Integer
            imageUrl: String
            isActive: Boolean
        }
        class BookVariant {
            id: Long
            price: Double
            stockQuantity: Integer
        }
        class Variant {
            id: Long
            formatCode: String
            formatName: String
        }
        class Category {
            id: Long
            code: String
            name: String
        }
        class BookEmbedding {
            id: Long
            vector: String
            model: String
            dimension: Integer
            textUsed: String
        }
        class Supplier {
            id: Long
            name: String
            email: String
            phone: String
            address: String
            isActive: Boolean
        }
    }

    namespace Order {
        class Cart {
            id: Long
        }
        class CartItem {
            id: Long
            quantity: Integer
            unitPrice: Double
        }
        class Order {
            id: Long
            orderDate: LocalDateTime
            totalAmount: Double
            status: OrderStatus
            paymentCode: String
            paymentMethod: PaymentMethod
        }
        class OrderDetail {
            id: Long
            priceAtPurchase: Double
            quantity: Integer
        }
    }

    namespace Marketing {
        class Promotion {
            id: Long
            name: String
            code: String
            discountPercent: Double
            startDate: LocalDate
            endDate: LocalDate
            quantity: Integer
            isActive: Boolean
            status: PromotionStatus
            priceOrderActive: Double
        }
        class Notification {
            id: Long
            title: String
            content: String
            createdAt: LocalDateTime
            isRead: Boolean
        }
        class Banner {
            id: Long
            name: String
            imageUrl: String
        }
    }

    namespace Inventory {
        class Batch {
            id: Long
            batchCode: String
            quantity: Integer
            remainingQuantity: Integer
            importPrice: Double
            productionDate: LocalDate
            createdAt: LocalDateTime
        }
        class BatchDetail {
            id: Long
            quantity: Integer
        }
        class ImportStock {
            id: Long
            importDate: LocalDateTime
            received: Boolean
        }
        class ImportStockDetail {
            id: Long
            quantity: Integer
            importPrice: Double
        }
    }

    namespace Procurement {
        class PurchaseOrder {
            id: Long
            createdAt: LocalDateTime
            approvedAt: LocalDateTime
            note: String
            cancelReason: String
            status: PurchaseOrderStatus
        }
        class PurchaseOrderItem {
            id: Long
            quantity: Integer
            importPrice: Double
        }
        class StockRequest {
            id: Long
            quantity: Integer
            reason: String
            status: StockRequestStatus
            createdAt: LocalDateTime
            processedAt: LocalDateTime
            responseMessage: String
        }
    }

    namespace Returns {
        class ReturnRequest {
            id: Long
            reason: String
            responseNote: String
            status: ReturnRequestStatus
            createdAt: LocalDateTime
            processedAt: LocalDateTime
        }
        class ReturnToWarehouseRequest {
            id: Long
            quantity: Integer
            reason: String
            responseNote: String
            status: ReturnToWarehouseRequestStatus
            createdAt: LocalDateTime
            processedAt: LocalDateTime
        }
        class DisposalRequest {
            id: Long
            reason: String
            responseNote: String
            status: DisposalRequestStatus
            createdAt: LocalDateTime
            processedAt: LocalDateTime
        }
        class DisposalRequestItem {
            id: Long
            quantity: Integer
            remainingQuantityAfter: Integer
        }
    }

    User "1" *-- "0..*" Address
    User "1" *-- "0..1" Cart
    User "1" *-- "0..*" RefreshToken
    User "1" *-- "0..*" VerificationToken
    User "1" *-- "0..*" Notification
    User "1" *-- "0..*" Order
    User "N" o-- "M" Role
    Role "N" o-- "M" Permission

    Book "N" o-- "1" Supplier
    Book "1" *-- "0..*" BookVariant
    Book "N" o-- "M" Category
    Book "1" *-- "0..1" BookEmbedding
    BookVariant "N" o-- "1" Variant
    Category "1" o-- "M" Book

    Cart "1" *-- "0..*" CartItem
    CartItem "N" o-- "1" Book
    Order "1" *-- "0..*" OrderDetail
    OrderDetail "N" o-- "1" Book
    OrderDetail "1" *-- "0..*" BatchDetail
    Order "N" o-- "1" Address
    Order "N" o-- "0..1" Promotion
    Order "N" o-- "1" User

    Promotion "1" *-- "0..*" Order
    Notification "N" o-- "1" User

    ImportStock "1" *-- "0..*" ImportStockDetail
    ImportStockDetail "1" *-- "0..*" Batch
    Batch "1" *-- "0..*" BatchDetail
    Batch "N" o-- "1" Supplier
    Batch "N" o-- "1" Book
    Batch "N" o-- "1" Variant
    Batch "N" o-- "1" User
    Batch "1" *-- "0..*" DisposalRequestItem

    PurchaseOrder "1" *-- "0..*" PurchaseOrderItem
    PurchaseOrderItem "N" o-- "1" Book
    PurchaseOrderItem "N" o-- "1" Variant
    PurchaseOrder "1" *-- "0..*" ImportStock
    PurchaseOrder "N" o-- "1" Supplier
    StockRequest "N" o-- "1" Book
    StockRequest "N" o-- "1" Variant
    StockRequest "N" o-- "1" User

    ReturnRequest "N" o-- "1" Order
    ReturnRequest "N" o-- "1" User
    ReturnToWarehouseRequest "N" o-- "1" Book
    ReturnToWarehouseRequest "N" o-- "1" User
    DisposalRequest "1" *-- "0..*" DisposalRequestItem
    DisposalRequest "N" o-- "1" User
    DisposalRequestItem "N" o-- "1" Batch
```

---

## Domain Summary

| # | Domain | Entities | Description |
|---|--------|----------|-------------|
| 1 | **Core** | User, Role, Permission, Address, RefreshToken, VerificationToken | Authentication, authorization, user management |
| 2 | **Catalog** | Book, BookVariant, Variant, Category, BookEmbedding, Supplier | Product catalog and suppliers |
| 3 | **Order** | Cart, CartItem, Order, OrderDetail | Shopping cart, order placement, order details |
| 4 | **Marketing** | Promotion, Notification, Banner | Promotions, notifications, banners |
| 5 | **Inventory** | Batch, BatchDetail, ImportStock, ImportStockDetail | Warehouse, stock batches, FIFO tracking |
| 6 | **Procurement** | PurchaseOrder, PurchaseOrderItem, StockRequest | Supplier ordering and stock requests |
| 7 | **Returns** | ReturnRequest, ReturnToWarehouseRequest, DisposalRequest, DisposalRequestItem | Customer returns, supplier returns, disposals |

---

## Key Design Patterns

### Domain-Driven Design (DDD)
Mỗi domain được tách biệt với các entity riêng. Cross-domain relationships chỉ qua foreign key references.

### Aggregate Roots
| Domain | Aggregate Root |
|--------|---------------|
| Core | User |
| Catalog | Book |
| Order | Order, Cart |
| Marketing | Promotion |
| Inventory | Batch, ImportStock |
| Procurement | PurchaseOrder, StockRequest |
| Returns | ReturnRequest, DisposalRequest |

### FIFO Inventory Tracking
- `Batch` là đơn vị tồn kho nhỏ nhất (theo supplier, book, variant, import batch)
- `BatchDetail` link OrderDetail với Batch theo thứ tự createdAt (FIFO)
- `Book.stockQuantity` = tổng `Batch.remainingQuantity` (tính toán qua service)

### CascadeDelete Strategy
- `User` → cascade xóa Address, RefreshToken, VerificationToken, Notification, Cart, CartItem
- `Order` → cascade xóa OrderDetail
- `DisposalRequest` → cascade xóa DisposalRequestItem
- Không cascade: Batch, PurchaseOrder, ReturnRequest (cần soft delete / status check)

---

## Related Documents

- **ER Diagram:** `er-diagram/er-001-full.md`
- **Domain Class Diagrams:**
  - `class-diagram/class-001-catalog.md`
  - `class-diagram/class-002-order.md`
  - `class-diagram/class-003-user.md`
  - `class-diagram/class-004-inventory.md`
  - `class-diagram/class-006-returns.md`
  - `class-diagram/class-007-marketing.md`
- **Use Case Overview:** `usecase/uc-001.md` — `usecase/uc-010.md`
- **Sequence Overview:** `sequence/seq-001.md` — `sequence/seq-010.md`

---

*Generated by Senior BA Agent | BookStore Backend | 2026-04-25*
