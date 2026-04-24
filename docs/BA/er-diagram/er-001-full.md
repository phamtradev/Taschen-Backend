# Full Entity-Relationship Diagram

> **Document ID:** er-001
> **Phiên bản:** 1.2.0
> **Ngày:** 2026-04-25

---

## Complete ER Diagram

```mermaid
erDiagram

USERS ||--o{ USER_ROLES : N_M
USERS ||--o{ ADDRESSES : 1_N
USERS ||--o| REFRESH_TOKENS : 1_N
USERS ||--o| VERIFICATION_TOKENS : 1_N
USERS ||--o{ NOTIFICATIONS : 1_N
USERS ||--o{ ORDERS : 1_N
USERS ||--o| USER_CARTS : 1_1

USERS {
    int id PK
    varchar first_name
    varchar last_name
    varchar gender
    varchar email UK NN
    varchar phone_number
    varchar is_active NN
    varchar password NN
}

ROLES ||--o{ ROLE_PERMISSIONS : 1_N
ROLES ||--o{ USER_ROLES : 1_N

ROLES {
    int id PK
    varchar code NN
    varchar name NN
}

PERMISSIONS ||--o{ ROLE_PERMISSIONS : 1_N

PERMISSIONS {
    int id PK
    varchar code UK NN
    varchar http_method NN
    varchar path_pattern NN
    varchar active NN
}

USER_ROLES {
    int user_id FK
    int role_id FK
}

ROLE_PERMISSIONS {
    int role_id FK
    int permission_id FK
}

ADDRESSES {
    int id PK
    varchar address_type
    varchar street
    varchar district
    varchar ward
    varchar city
    varchar recipient_name
    varchar phone_number
    varchar is_default
    int user_id FK NN
}

REFRESH_TOKENS {
    int id PK
    varchar token NN
    int user_id FK NN
    datetime expires_at NN
    varchar revoked NN
    datetime created_at NN
}

VERIFICATION_TOKENS {
    int id PK
    varchar token UK NN
    int user_id FK NN
    datetime expires_at
}

BOOKS ||--o{ BOOK_VARIANTS : 1_N
BOOKS ||--o{ BOOK_CATEGORIES : 1_N
BOOKS ||--o{ BATCHES : 1_N
BOOKS ||--o{ IMPORT_STOCK_DETAILS : 1_N
BOOKS ||--o| BOOK_EMBEDDINGS : 1_1
BOOKS }o--|| SUPPLIERS : N_1

BOOKS {
    int id PK
    varchar title NN
    varchar author
    text description
    int publication_year
    int weight_grams
    int page_count
    int price NN
    int stock_quantity NN
    varchar image_url
    varchar is_active
    int supplier_id FK
}

BOOK_VARIANTS ||--|| VARIANTS : N_1

BOOK_VARIANTS {
    int id PK
    int book_id FK NN
    int variant_id FK NN
    int price NN
    int stock_quantity NN
}

VARIANTS {
    int id PK
    varchar format_code
    varchar format_name
}

CATEGORIES ||--o{ BOOK_CATEGORIES : 1_N

CATEGORIES {
    int id PK
    varchar code UK NN
    varchar name NN
}

BOOK_CATEGORIES {
    int book_id FK
    int category_id FK
}

BOOK_EMBEDDINGS {
    int id PK
    int book_id UK FK NN
    json vector
    varchar model
    int dimension
    text text_used
}

SUPPLIERS ||--o{ BOOKS : 1_N
SUPPLIERS ||--o{ BATCHES : 1_N
SUPPLIERS ||--o{ PURCHASE_ORDERS : 1_N
SUPPLIERS ||--o{ IMPORT_STOCKS : 1_N
SUPPLIERS ||--o{ IMPORT_STOCK_DETAILS : 1_N

SUPPLIERS {
    int id PK
    varchar name NN
    varchar email
    varchar phone
    varchar address
    varchar is_active NN
}

USER_CARTS ||--o{ CART_ITEMS : 1_N
USER_CARTS ||--|| USERS : 1_1

USER_CARTS {
    int id PK
    int user_id FK UK NN
}

CART_ITEMS ||--|| USER_CARTS : N_1
CART_ITEMS }o--|| BOOKS : N_1

CART_ITEMS {
    int id PK
    int quantity NN
    int unit_price NN
    int cart_id FK NN
    int book_id FK NN
}

ORDERS ||--o{ ORDER_DETAILS : 1_N
ORDERS }o--|| USERS : N_1
ORDERS }o--|| ADDRESSES : N_1
ORDERS }o--|| PROMOTIONS : N_1

ORDERS {
    int id PK
    datetime order_date NN
    int total_amount NN
    varchar status NN
    varchar payment_code
    varchar payment_method
    int user_id FK NN
    int promotion_id FK
    int address_id FK NN
}

ORDER_DETAILS ||--|| ORDERS : N_1
ORDER_DETAILS }o--|| BOOKS : N_1
ORDER_DETAILS ||--o{ BATCH_DETAILS : 1_N

ORDER_DETAILS {
    int id PK
    int price_at_purchase NN
    int quantity NN
    int order_id FK NN
    int book_id FK NN
}

PROMOTIONS ||--o{ ORDERS : 1_N
PROMOTIONS }o--|| USERS : N_1

PROMOTIONS {
    int id PK
    varchar name NN
    varchar code UK NN
    int discount_percent NN
    date start_date NN
    date end_date NN
    int quantity NN
    varchar is_active
    varchar status NN
    int price_order_active
    int created_by_user_id FK NN
    int approved_by_user_id FK
}

NOTIFICATIONS ||--|| USERS : N_1
NOTIFICATIONS }o--|| USERS : N_1

NOTIFICATIONS {
    int id PK
    varchar title NN
    text content
    datetime created_at NN
    varchar is_read NN
    int sender_user_id FK
    int receiver_user_id FK NN
}

BATCHES ||--o{ BATCH_DETAILS : 1_N
BATCHES }o--|| BOOKS : N_1
BATCHES }o--|| VARIANTS : N_1
BATCHES }o--|| SUPPLIERS : N_1
BATCHES }o--|| USERS : N_1
BATCHES }o--|| IMPORT_STOCK_DETAILS : N_1
BATCHES ||--o{ DISPOSAL_REQUEST_ITEMS : 1_N

BATCHES {
    int id PK
    varchar batch_code UK NN
    int quantity NN
    int remaining_quantity NN
    int import_price NN
    date production_date
    datetime created_at NN
    int supplier_id FK NN
    int created_by_id FK NN
    int book_id FK NN
    int variant_id FK NN
    int import_stock_detail_id FK
}

BATCH_DETAILS ||--|| BATCHES : N_1
BATCH_DETAILS }o--|| ORDER_DETAILS : N_1

BATCH_DETAILS {
    int id PK
    int quantity NN
    int batch_id FK NN
    int order_detail_id FK NN
}

IMPORT_STOCKS ||--o{ IMPORT_STOCK_DETAILS : 1_N
IMPORT_STOCKS }o--|| USERS : N_1
IMPORT_STOCKS }o--|| SUPPLIERS : N_1
IMPORT_STOCKS }o--|| PURCHASE_ORDERS : N_1

IMPORT_STOCKS {
    int id PK
    datetime import_date NN
    varchar received NN
    int created_by_id FK NN
    int supplier_id FK NN
    int purchase_order_id FK
}

IMPORT_STOCK_DETAILS ||--|| IMPORT_STOCKS : N_1
IMPORT_STOCK_DETAILS ||--o{ BATCHES : 1_N
IMPORT_STOCK_DETAILS }o--|| BOOKS : N_1
IMPORT_STOCK_DETAILS }o--|| VARIANTS : N_1
IMPORT_STOCK_DETAILS }o--|| SUPPLIERS : N_1

IMPORT_STOCK_DETAILS {
    int id PK
    int quantity NN
    int import_price NN
    int import_stock_id FK NN
    int book_id FK NN
    int variant_id FK NN
    int supplier_id FK NN
}

PURCHASE_ORDERS ||--o{ PURCHASE_ORDER_ITEMS : 1_N
PURCHASE_ORDERS }o--|| SUPPLIERS : N_1
PURCHASE_ORDERS }o--|| USERS : N_1
PURCHASE_ORDERS ||--o| IMPORT_STOCKS : 1_N

PURCHASE_ORDERS {
    int id PK
    datetime created_at NN
    datetime approved_at
    varchar note
    varchar cancel_reason
    varchar status NN
    int supplier_id FK NN
    int created_by_user_id FK NN
    int approved_by_user_id FK
}

PURCHASE_ORDER_ITEMS ||--|| PURCHASE_ORDERS : N_1
PURCHASE_ORDER_ITEMS }o--|| BOOKS : N_1
PURCHASE_ORDER_ITEMS }o--|| VARIANTS : N_1

PURCHASE_ORDER_ITEMS {
    int id PK
    int quantity NN
    int import_price NN
    int book_id FK NN
    int variant_id FK NN
    int purchase_order_id FK NN
}

STOCK_REQUESTS }o--|| BOOKS : N_1
STOCK_REQUESTS }o--|| VARIANTS : N_1
STOCK_REQUESTS }o--|| USERS : N_1

STOCK_REQUESTS {
    int id PK
    int quantity NN
    varchar reason
    varchar status NN
    datetime created_at NN
    datetime processed_at
    varchar response_message
    int book_id FK NN
    int variant_id FK
    int created_by_user_id FK NN
    int processed_by_user_id FK
}

RETURN_REQUESTS ||--|| ORDERS : N_1
RETURN_REQUESTS }o--|| USERS : N_1

RETURN_REQUESTS {
    int id PK
    varchar reason
    varchar response_note
    varchar status NN
    datetime created_at NN
    datetime processed_at
    int order_id FK NN
    int created_by_user_id FK NN
    int processed_by_user_id FK
}

RETURN_TO_WAREHOUSE_REQUESTS }o--|| BOOKS : N_1
RETURN_TO_WAREHOUSE_REQUESTS }o--|| USERS : N_1

RETURN_TO_WAREHOUSE_REQUESTS {
    int id PK
    int quantity NN
    varchar reason
    varchar response_note
    varchar status NN
    datetime created_at NN
    datetime processed_at
    int created_by_user_id FK NN
    int processed_by_user_id FK
    int book_id FK NN
}

DISPOSAL_REQUESTS ||--o{ DISPOSAL_REQUEST_ITEMS : 1_N
DISPOSAL_REQUESTS }o--|| USERS : N_1

DISPOSAL_REQUESTS {
    int id PK
    varchar reason
    varchar response_note
    varchar status NN
    datetime created_at NN
    datetime processed_at
    int created_by_user_id FK NN
    int processed_by_user_id FK
}

DISPOSAL_REQUEST_ITEMS ||--|| DISPOSAL_REQUESTS : N_1
DISPOSAL_REQUEST_ITEMS }o--|| BATCHES : N_1

DISPOSAL_REQUEST_ITEMS {
    int id PK
    int quantity NN
    int remaining_quantity_after
    int disposal_request_id FK NN
    int batch_id FK NN
}

BANNERS {
    int id PK
    varchar name NN
    varchar image_url NN
}
```

---

## Database Table Summary

| # | Table | Entity | Domain | Description |
|---|-------|--------|--------|-------------|
| 1 | `users` | User | Core | User accounts |
| 2 | `roles` | Role | Core | User roles |
| 3 | `permissions` | Permission | Core | API permissions |
| 4 | `user_roles` | - | Core | User-Role mapping |
| 5 | `role_permissions` | - | Core | Role-Permission mapping |
| 6 | `addresses` | Address | Core | Delivery addresses |
| 7 | `refresh_tokens` | RefreshToken | Core | JWT refresh tokens |
| 8 | `verification_tokens` | VerificationToken | Core | Email verification tokens |
| 9 | `books` | Book | Catalog | Book products |
| 10 | `variants` | Variant | Catalog | Book format variants |
| 11 | `book_variants` | BookVariant | Catalog | Book-variant combinations |
| 12 | `categories` | Category | Catalog | Book categories |
| 13 | `book_categories` | - | Catalog | Book-Category mapping |
| 14 | `book_embeddings` | BookEmbedding | Catalog | AI embeddings |
| 15 | `suppliers` | Supplier | Catalog | Suppliers |
| 16 | `user_carts` | Cart | Order | Shopping carts |
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
| 30 | `return_to_warehouse_requests` | ReturnToWarehouseRequest | Returns | Seller returns to supplier |
| 31 | `disposal_requests` | DisposalRequest | Returns | Disposal requests |
| 32 | `disposal_request_items` | DisposalRequestItem | Returns | Disposal line items |
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

## Data Type Mapping

| Database Type | Mermaid Type | Notes |
|---------------|-------------|-------|
| `BIGINT` | `int` | Mermaid only supports `int` for integers |
| `VARCHAR` | `varchar` | Character strings |
| `TEXT` | `text` | Long text fields |
| `BOOLEAN` | `varchar` | Stored as string (`true`/`false`) |
| `DOUBLE` | `int` | Stored as integer (scaled) |
| `DATETIME` | `datetime` | Date and time |
| `DATE` | `date` | Date only |
| `JSON` | `json` | JSON data |

> Mermaid ER diagrams only support 5 native data types: `int`, `varchar`, `text`, `datetime`, `date`. All other types are mapped to the closest supported type.

---

*Generated by Senior BA Agent | BookStore Backend | 2026-04-25*
