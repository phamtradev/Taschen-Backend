# Full Entity-Relationship Diagram

> **Document ID:** er-001
> **Phiên bản:** 1.0.0
> **Ngày:** 2026-04-25

---

## Complete ER Diagram

```mermaid
erDiagram
    %% === USER DOMAIN ===
    USERS ||--o| USER_ROLES : "N:M"
    USERS ||--o{ ADDRESSES : "1:N" }
    USERS ||--o| REFRESH_TOKENS : "1:N"
    USERS ||--o| VERIFICATION_TOKENS : "1:N"
    USERS ||--o{ NOTIFICATIONS : "1:N" }
    USERS ||--o{ ORDERS : "1:N" }
    USERS ||--o{ USER_CARTS : "1:1" }

    USERS {
        bigint id PK
        varchar first_name
        varchar last_name
        varchar gender
        varchar email UK NN
        varchar phone_number
        bool is_active NN
        varchar password NN
    }

    ROLES ||--o| ROLE_PERMISSIONS : "1:N"
    ROLES ||--o| USER_ROLES : "1:N"

    ROLES {
        bigint id PK
        varchar code NN
        varchar name NN
    }

    PERMISSIONS ||--o| ROLE_PERMISSIONS : "1:N"

    PERMISSIONS {
        bigint id PK
        varchar code UK NN
        varchar http_method NN
        varchar path_pattern NN
        bool active NN
    }

    USER_ROLES {
        bigint user_id FK
        bigint role_id FK
    }

    ROLE_PERMISSIONS {
        bigint role_id FK
        bigint permission_id FK
    }

    ADDRESSES {
        bigint id PK
        varchar address_type
        varchar street
        varchar district
        varchar ward
        varchar city
        varchar recipient_name
        varchar phone_number
        bool is_default
        bigint user_id FK NN
    }

    REFRESH_TOKENS {
        bigint id PK
        varchar token NN
        bigint user_id FK NN
        timestamp expires_at NN
        bool revoked NN
        timestamp created_at NN
    }

    VERIFICATION_TOKENS {
        bigint id PK
        varchar token UK NN
        bigint user_id FK NN
        timestamp expires_at
    }

    %% === CATALOG DOMAIN ===
    BOOKS ||--o{ BOOK_VARIANTS : "1:N"
    BOOKS ||--o{ BOOK_CATEGORIES : "1:N"
    BOOKS ||--o{ BOOK_CATEGORIES : "M:N"
    BOOKS ||--o{ BATCHES : "1:N"
    BOOKS ||--o{ IMPORT_STOCK_DETAILS : "1:N"
    BOOKS ||--o{ BOOK_EMBEDDINGS : "1:1"
    BOOKS }o--|| SUPPLIERS : "N:1"

    BOOKS {
        bigint id PK
        varchar title NN
        varchar author
        text description
        int publication_year
        int weight_grams
        int page_count
        double price NN
        int stock_quantity NN
        varchar image_url
        bool is_active
        bigint supplier_id FK
    }

    BOOK_VARIANTS {
        bigint id PK
        bigint book_id FK NN
        bigint variant_id FK NN
        double price NN
        int stock_quantity NN
    }

    BOOK_VARIANTS }o--|| VARIANTS : "N:1"

    VARIANTS {
        bigint id PK
        varchar format_code
        varchar format_name
    }

    CATEGORIES ||--o{ BOOK_CATEGORIES : "1:N"

    CATEGORIES {
        bigint id PK
        varchar code UK NN
        varchar name NN
    }

    BOOK_CATEGORIES {
        bigint book_id FK
        bigint category_id FK
    }

    BOOK_EMBEDDINGS {
        bigint id PK
        bigint book_id UK FK NN
        json vector
        varchar model
        int dimension
        text text_used
    }

    SUPPLIERS ||--o{ BOOKS : "1:N"
    SUPPLIERS ||--o{ BATCHES : "1:N"
    SUPPLIERS ||--o{ PURCHASE_ORDERS : "1:N"
    SUPPLIERS ||--o{ IMPORT_STOCKS : "1:N"
    SUPPLIERS ||--o{ IMPORT_STOCK_DETAILS : "1:N"

    SUPPLIERS {
        bigint id PK
        varchar name NN
        varchar email
        varchar phone
        varchar address
        bool is_active NN
    }

    %% === ORDER DOMAIN ===
    USER_CARTS ||--o{ CART_ITEMS : "1:N"
    USER_CARTS }o--|| USERS : "1:1"

    CARTS {
        bigint id PK
        double total_price
        bigint user_id FK UK NN
    }

    CART_ITEMS ||--o|| CARTS : "N:1"
    CART_ITEMS }o--|| BOOKS : "N:1"

    CART_ITEMS {
        bigint id PK
        int quantity NN
        double unit_price NN
        bigint cart_id FK NN
        bigint book_id FK NN
    }

    ORDERS ||--o{ ORDER_DETAILS : "1:N"
    ORDERS }o--|| USERS : "N:1"
    ORDERS }o--|| ADDRESSES : "N:1"
    ORDERS }o--|| PROMOTIONS : "N:1"

    ORDERS {
        bigint id PK
        datetime order_date NN
        double total_amount NN
        varchar status NN
        varchar payment_code
        varchar payment_method
        bigint user_id FK NN
        bigint promotion_id FK
        bigint address_id FK NN
    }

    ORDER_DETAILS ||--o|| ORDERS : "N:1"
    ORDER_DETAILS }o--|| BOOKS : "N:1"
    ORDER_DETAILS ||--o{ BATCH_DETAILS : "1:N"

    ORDER_DETAILS {
        bigint id PK
        double price_at_purchase NN
        int quantity NN
        bigint order_id FK NN
        bigint book_id FK NN
    }

    %% === PROMOTION DOMAIN ===
    PROMOTIONS ||--o{ ORDERS : "1:N"
    PROMOTIONS }o--|| USERS : "N:1"

    PROMOTIONS {
        bigint id PK
        varchar name NN
        varchar code UK NN
        double discount_percent NN
        date start_date NN
        date end_date NN
        int quantity NN
        bool is_active
        varchar status NN
        double price_order_active
        bigint created_by_user_id FK NN
        bigint approved_by_user_id FK
    }

    %% === NOTIFICATION DOMAIN ===
    NOTIFICATIONS ||--o|| USERS : "N:1"
    NOTIFICATIONS }o--|| USERS : "N:1"

    NOTIFICATIONS {
        bigint id PK
        varchar title NN
        text content
        datetime created_at NN
        bool is_read NN
        bigint sender_user_id FK
        bigint receiver_user_id FK NN
    }

    %% === INVENTORY DOMAIN ===
    BATCHES ||--o{ BATCH_DETAILS : "1:N" }
    BATCHES }o--|| BOOKS : "N:1"
    BATCHES }o--|| VARIANTS : "N:1"
    BATCHES }o--|| SUPPLIERS : "N:1"
    BATCHES }o--|| USERS : "N:1"
    BATCHES }o--|| IMPORT_STOCK_DETAILS : "N:1"
    BATCHES ||--o{ DISPOSAL_REQUEST_ITEMS : "1:N" }

    BATCHES {
        bigint id PK
        varchar batch_code UK NN
        int quantity NN
        int remaining_quantity NN
        double import_price NN
        date production_date
        datetime created_at NN
        bigint supplier_id FK NN
        bigint created_by_id FK NN
        bigint book_id FK NN
        bigint variant_id FK NN
        bigint import_stock_detail_id FK
    }

    BATCH_DETAILS ||--o|| BATCHES : "N:1"
    BATCH_DETAILS }o--|| ORDER_DETAILS : "N:1"

    BATCH_DETAILS {
        bigint id PK
        int quantity NN
        bigint batch_id FK NN
        bigint order_detail_id FK NN
    }

    IMPORT_STOCKS ||--o{ IMPORT_STOCK_DETAILS : "1:N"
    IMPORT_STOCKS }o--|| USERS : "N:1"
    IMPORT_STOCKS }o--|| SUPPLIERS : "N:1"
    IMPORT_STOCKS }o--|| PURCHASE_ORDERS : "N:1"

    IMPORT_STOCKS {
        bigint id PK
        datetime import_date NN
        bool received NN
        bigint created_by_id FK NN
        bigint supplier_id FK NN
        bigint purchase_order_id FK
    }

    IMPORT_STOCK_DETAILS ||--o|| IMPORT_STOCKS : "N:1"
    IMPORT_STOCK_DETAILS ||--o{ BATCHES : "1:N" }
    IMPORT_STOCK_DETAILS }o--|| BOOKS : "N:1"
    IMPORT_STOCK_DETAILS }o--|| VARIANTS : "N:1"
    IMPORT_STOCK_DETAILS }o--|| SUPPLIERS : "N:1"

    IMPORT_STOCK_DETAILS {
        bigint id PK
        int quantity NN
        double import_price NN
        bigint import_stock_id FK NN
        bigint book_id FK NN
        bigint variant_id FK NN
        bigint supplier_id FK NN
    }

    %% === PROCUREMENT DOMAIN ===
    PURCHASE_ORDERS ||--o{ PURCHASE_ORDER_ITEMS : "1:N"
    PURCHASE_ORDERS }o--|| SUPPLIERS : "N:1"
    PURCHASE_ORDERS }o--|| USERS : "N:1"
    PURCHASE_ORDERS ||--o| IMPORT_STOCKS : "1:N"

    PURCHASE_ORDERS {
        bigint id PK
        datetime created_at NN
        datetime approved_at
        varchar note
        varchar cancel_reason
        varchar status NN
        bigint supplier_id FK NN
        bigint created_by_user_id FK NN
        bigint approved_by_user_id FK
    }

    PURCHASE_ORDER_ITEMS ||--o|| PURCHASE_ORDERS : "N:1"
    PURCHASE_ORDER_ITEMS }o--|| BOOKS : "N:1"
    PURCHASE_ORDER_ITEMS }o--|| VARIANTS : "N:1"

    PURCHASE_ORDER_ITEMS {
        bigint id PK
        int quantity NN
        double import_price NN
        bigint book_id FK NN
        bigint variant_id FK NN
        bigint purchase_order_id FK NN
    }

    STOCK_REQUESTS }o--|| BOOKS : "N:1"
    STOCK_REQUESTS }o--|| VARIANTS : "N:1"
    STOCK_REQUESTS }o--|| USERS : "N:1"

    STOCK_REQUESTS {
        bigint id PK
        int quantity NN
        varchar reason
        varchar status NN
        datetime created_at NN
        datetime processed_at
        varchar response_message
        bigint book_id FK NN
        bigint variant_id FK
        bigint created_by_user_id FK NN
        bigint processed_by_user_id FK
    }

    %% === RETURN DOMAIN ===
    RETURN_REQUESTS ||--o|| ORDERS : "N:1"
    RETURN_REQUESTS }o--|| USERS : "N:1"

    RETURN_REQUESTS {
        bigint id PK
        varchar reason
        varchar response_note
        varchar status NN
        datetime created_at NN
        datetime processed_at
        bigint order_id FK NN
        bigint created_by_user_id FK NN
        bigint processed_by_user_id FK
    }

    RETURN_TO_WAREHOUSE_REQUESTS }o--|| BOOKS : "N:1"
    RETURN_TO_WAREHOUSE_REQUESTS }o--|| USERS : "N:1"

    RETURN_TO_WAREHOUSE_REQUESTS {
        bigint id PK
        int quantity NN
        varchar reason
        varchar response_note
        varchar status NN
        datetime created_at NN
        datetime processed_at
        bigint created_by_user_id FK NN
        bigint processed_by_user_id FK
        bigint book_id FK NN
    }

    DISPOSAL_REQUESTS ||--o{ DISPOSAL_REQUEST_ITEMS : "1:N"
    DISPOSAL_REQUESTS }o--|| USERS : "N:1"

    DISPOSAL_REQUESTS {
        bigint id PK
        varchar reason
        varchar response_note
        varchar status NN
        datetime created_at NN
        datetime processed_at
        bigint created_by_user_id FK NN
        bigint processed_by_user_id FK
    }

    DISPOSAL_REQUEST_ITEMS ||--o|| DISPOSAL_REQUESTS : "N:1"
    DISPOSAL_REQUEST_ITEMS }o--|| BATCHES : "N:1"

    DISPOSAL_REQUEST_ITEMS {
        bigint id PK
        int quantity NN
        int remaining_quantity_after
        bigint disposal_request_id FK NN
        bigint batch_id FK NN
    }

    %% === BANNER DOMAIN ===
    BANNERS {
        bigint id PK
        varchar name NN
        varchar image_url NN
    }
```

---

## Database Table Summary

| # | Table | Entity | Type | Description |
|---|-------|--------|------|-------------|
| 1 | `users` | User | Core | User accounts |
| 2 | `roles` | Role | Core | User roles |
| 3 | `permissions` | Permission | Core | API permissions |
| 4 | `user_roles` | - | Join | User-Role mapping |
| 5 | `role_permissions` | - | Join | Role-Permission mapping |
| 6 | `addresses` | Address | Core | Delivery addresses |
| 7 | `refresh_tokens` | RefreshToken | Core | JWT refresh tokens |
| 8 | `verification_tokens` | VerificationToken | Core | Email verification |
| 9 | `books` | Book | Catalog | Book products |
| 10 | `variants` | Variant | Catalog | Book formats |
| 11 | `book_variants` | BookVariant | Catalog | Book-format combo |
| 12 | `categories` | Category | Catalog | Book categories |
| 13 | `book_categories` | - | Join | Book-Category mapping |
| 14 | `book_embeddings` | BookEmbedding | Catalog | AI embeddings |
| 15 | `suppliers` | Supplier | Catalog | Suppliers |
| 16 | `carts` | Cart | Order | Shopping carts |
| 17 | `cart_items` | CartItem | Order | Cart line items |
| 18 | `orders` | Order | Order | Customer orders |
| 19 | `order_details` | OrderDetail | Order | Order line items |
| 20 | `promotions` | Promotion | Marketing | Promotions |
| 21 | `notifications` | Notification | Marketing | User notifications |
| 22 | `batches` | Batch | Inventory | Inventory batches |
| 23 | `batch_details` | BatchDetail | Inventory | Batch-Order mapping |
| 24 | `import_stocks` | ImportStock | Inventory | Import receipts |
| 25 | `import_stock_details` | ImportStockDetail | Inventory | Import line items |
| 26 | `purchase_orders` | PurchaseOrder | Procurement | Supplier orders |
| 27 | `purchase_order_items` | PurchaseOrderItem | Procurement | PO line items |
| 28 | `stock_requests` | StockRequest | Procurement | Stock requests |
| 29 | `return_requests` | ReturnRequest | Returns | Customer returns |
| 30 | `return_to_warehouse_requests` | ReturnToWarehouseRequest | Returns | Seller returns |
| 31 | `disposal_requests` | DisposalRequest | Returns | Disposals |
| 32 | `disposal_request_items` | DisposalRequestItem | Returns | Disposal items |
| 33 | `banners` | Banner | Marketing | Homepage banners |

**Total: 33 tables**

---

## Join Tables

| Join Table | From | To | Type |
|------------|------|----|------|
| `user_roles` | User | Role | M:N |
| `role_permissions` | Role | Permission | M:N |
| `book_categories` | Book | Category | M:N |

---

*Generated by Senior BA Agent | BookStore Backend | 2026-04-25*
