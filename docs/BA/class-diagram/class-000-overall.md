# Overall Class Diagram

> **Document ID:** class-000
> **Phiên bản:** 1.0.0
> **Ngày:** 2026-04-25
> **Phạm vi:** Toàn hệ thống — 33 entities, 6 domains

---

## Complete Class Diagram

```mermaid
classDiagram
    direction TB
    <<package>> Core

    class User {
        id: Long
        firstName: String
        lastName: String
        gender: Gender
        email: String
        phoneNumber: String
        isActive: Boolean
        password: String
        roles: Set~Role~
        addresses: List~Address~
        cart: Cart
        refreshTokens: List~RefreshToken~
        verificationTokens: List~VerificationToken~
        notifications: List~Notification~
        orders: List~Order~
    }

    class Role {
        id: Long
        code: String
        name: String
        permissions: Set~Permission~
        users: Set~User~
    }

    class Permission {
        id: Long
        code: String
        httpMethod: HttpMethod
        pathPattern: String
        active: Boolean
        roles: Set~Role~
    }

    class UserRole {
        userId: Long
        roleId: Long
    }

    class RolePermission {
        roleId: Long
        permissionId: Long
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
        user: User
    }

    class RefreshToken {
        id: Long
        token: String
        expiresAt: Instant
        revoked: Boolean
        createdAt: Instant
        user: User
    }

    class VerificationToken {
        id: Long
        token: String
        expiresAt: Instant
        user: User
    }

    %% ========== CATALOG ==========
    <<package>> Catalog

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
        supplier: Supplier
        bookVariants: List~BookVariant~
        categories: Set~Category~
        batches: List~Batch~
        bookEmbedding: BookEmbedding
    }

    class BookVariant {
        id: Long
        price: Double
        stockQuantity: Integer
        book: Book
        variant: Variant
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
        books: Set~Book~
    }

    class BookCategory {
        bookId: Long
        categoryId: Long
    }

    class BookEmbedding {
        id: Long
        bookId: Long
        vector: String
        model: String
        dimension: Integer
        textUsed: String
        book: Book
    }

    class Supplier {
        id: Long
        name: String
        email: String
        phone: String
        address: String
        isActive: Boolean
        books: List~Book~
        batches: List~Batch~
        purchaseOrders: List~PurchaseOrder~
        importStocks: List~ImportStock~
    }

    %% ========== ORDER ==========
    <<package>> Order

    class Cart {
        id: Long
        user: User
        items: List~CartItem~
    }

    class CartItem {
        id: Long
        quantity: Integer
        unitPrice: Double
        cart: Cart
        book: Book
    }

    class Order {
        id: Long
        orderDate: LocalDateTime
        totalAmount: Double
        status: OrderStatus
        paymentCode: String
        paymentMethod: PaymentMethod
        user: User
        address: Address
        promotion: Promotion
        orderDetails: List~OrderDetail~
    }

    class OrderDetail {
        id: Long
        priceAtPurchase: Double
        quantity: Integer
        order: Order
        book: Book
        batchDetails: List~BatchDetail~
    }

    %% ========== MARKETETING ==========
    <<package>> Marketing

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
        createdBy: User
        approvedBy: User
        orders: List~Order~
    }

    class Notification {
        id: Long
        title: String
        content: String
        createdAt: LocalDateTime
        isRead: Boolean
        sender: User
        receiver: User
    }

    class Banner {
        id: Long
        name: String
        imageUrl: String
    }

    %% ========== INVENTORY ==========
    <<package>> Inventory

    class Batch {
        id: Long
        batchCode: String
        quantity: Integer
        remainingQuantity: Integer
        importPrice: Double
        productionDate: LocalDate
        createdAt: LocalDateTime
        supplier: Supplier
        createdBy: User
        book: Book
        variant: Variant
        importStockDetail: ImportStockDetail
        batchDetails: List~BatchDetail~
        disposalItems: List~DisposalRequestItem~
    }

    class BatchDetail {
        id: Long
        quantity: Integer
        batch: Batch
        orderDetail: OrderDetail
    }

    class ImportStock {
        id: Long
        importDate: LocalDateTime
        received: Boolean
        createdBy: User
        supplier: Supplier
        purchaseOrder: PurchaseOrder
        importStockDetails: List~ImportStockDetail~
    }

    class ImportStockDetail {
        id: Long
        quantity: Integer
        importPrice: Double
        importStock: ImportStock
        book: Book
        variant: Variant
        supplier: Supplier
        batches: List~Batch~
    }

    %% ========== PROCUREMENT ==========
    <<package>> Procurement

    class PurchaseOrder {
        id: Long
        createdAt: LocalDateTime
        approvedAt: LocalDateTime
        note: String
        cancelReason: String
        status: PurchaseOrderStatus
        supplier: Supplier
        createdBy: User
        approvedBy: User
        purchaseOrderItems: List~PurchaseOrderItem~
        importStocks: List~ImportStock~
    }

    class PurchaseOrderItem {
        id: Long
        quantity: Integer
        importPrice: Double
        book: Book
        variant: Variant
        purchaseOrder: PurchaseOrder
    }

    class StockRequest {
        id: Long
        quantity: Integer
        reason: String
        status: StockRequestStatus
        createdAt: LocalDateTime
        processedAt: LocalDateTime
        responseMessage: String
        book: Book
        variant: Variant
        createdBy: User
        processedBy: User
    }

    %% ========== RETURNS ==========
    <<package>> Returns

    class ReturnRequest {
        id: Long
        reason: String
        responseNote: String
        status: ReturnRequestStatus
        createdAt: LocalDateTime
        processedAt: LocalDateTime
        order: Order
        createdBy: User
        processedBy: User
    }

    class ReturnToWarehouseRequest {
        id: Long
        quantity: Integer
        reason: String
        responseNote: String
        status: ReturnToWarehouseRequestStatus
        createdAt: LocalDateTime
        processedAt: LocalDateTime
        book: Book
        createdBy: User
        processedBy: User
    }

    class DisposalRequest {
        id: Long
        reason: String
        responseNote: String
        status: DisposalRequestStatus
        createdAt: LocalDateTime
        processedAt: LocalDateTime
        createdBy: User
        processedBy: User
        items: List~DisposalRequestItem~
    }

    class DisposalRequestItem {
        id: Long
        quantity: Integer
        remainingQuantityAfter: Integer
        disposalRequest: DisposalRequest
        batch: Batch
    }

    %% ---- RELATIONSHIPS ----
    %% USER domain
    User "1" *-- "0..*" Address : has
    User "1" *-- "0..1" Cart : owns
    User "1" *-- "0..*" RefreshToken : owns
    User "1" *-- "0..*" VerificationToken : owns
    User "1" *-- "0..*" Notification : receives
    User "1" *-- "0..*" Order : places
    User "N" o-- "M" Role : assigned to
    Role "N" o-- "M" Permission : has

    %% CATALOG domain
    Book "N" o-- "1" Supplier : supplied by
    Book "1" *-- "0..*" BookVariant : has
    Book "N" o-- "M" Category : categorized as
    Book "1" *-- "0..1" BookEmbedding : has
    BookVariant "N" o-- "1" Variant : is a
    Category "1" o-- "M" Book : contains

    %% ORDER domain
    Cart "1" *-- "0..*" CartItem : contains
    CartItem "N" o-- "1" Book : contains
    Order "1" *-- "0..*" OrderDetail : contains
    OrderDetail "N" o-- "1" Book : references
    OrderDetail "1" *-- "0..*" BatchDetail : allocated from
    Order "N" o-- "1" Address : delivered to
    Order "N" o-- "0..1" Promotion : applies
    Order "N" o-- "1" User : belongs to

    %% MARKETING domain
    Promotion "1" *-- "0..*" Order : applied to
    Notification "N" o-- "1" User : receiver
    Notification "N" o-- "0..1" User : sender

    %% INVENTORY domain
    ImportStock "1" *-- "0..*" ImportStockDetail : contains
    ImportStockDetail "1" *-- "0..*" Batch : creates
    ImportStockDetail "N" o-- "1" Supplier : from
    ImportStockDetail "N" o-- "1" Book : contains
    ImportStockDetail "N" o-- "1" Variant : format
    Batch "1" *-- "0..*" BatchDetail : tracks usage
    BatchDetail "N" o-- "1" OrderDetail : fulfills
    Batch "N" o-- "1" Supplier : from
    Batch "N" o-- "1" Book : contains
    Batch "N" o-- "1" Variant : format
    Batch "N" o-- "1" User : created by
    Batch "N" o-- "0..1" ImportStockDetail : linked to
    Batch "1" *-- "0..*" DisposalRequestItem : tracked in

    %% PROCUREMENT domain
    PurchaseOrder "1" *-- "0..*" PurchaseOrderItem : contains
    PurchaseOrderItem "N" o-- "1" Book : contains
    PurchaseOrderItem "N" o-- "1" Variant : format
    PurchaseOrder "1" *-- "0..*" ImportStock : sourced to
    PurchaseOrder "N" o-- "1" Supplier : from
    StockRequest "N" o-- "1" Book : requests
    StockRequest "N" o-- "1" Variant : for format
    StockRequest "N" o-- "1" User : created by

    %% RETURNS domain
    ReturnRequest "N" o-- "1" Order : for order
    ReturnRequest "N" o-- "1" User : created by
    ReturnRequest "N" o-- "0..1" User : processed by
    ReturnToWarehouseRequest "N" o-- "1" Book : book returned
    ReturnToWarehouseRequest "N" o-- "1" User : created by
    ReturnToWarehouseRequest "N" o-- "0..1" User : processed by
    DisposalRequest "1" *-- "0..*" DisposalRequestItem : contains
    DisposalRequest "N" o-- "1" User : created by
    DisposalRequest "N" o-- "0..1" User : processed by
    DisposalRequestItem "N" o-- "1" Batch : batch reference
```

---

## Domain Summary

| # | Domain | Entities | Description |
|---|--------|----------|-------------|
| 1 | **Core** | User, Role, Permission, UserRole, RolePermission, Address, RefreshToken, VerificationToken | Authentication, authorization, user management |
| 2 | **Catalog** | Book, BookVariant, Variant, Category, BookCategory, BookEmbedding, Supplier | Product catalog and suppliers |
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
