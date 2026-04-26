# System Architecture Overview

> **Document ID:** arch-001
> **Phiên bản:** 2.0.0
> **Ngày:** 2026-04-25

---

## 1. System Overview

Hệ thống **BookStore E-Commerce** là một ứng dụng thương mại điện tử bán sách, được xây dựng theo kiến trúc **Full-Stack** với:

- **Frontend:** Next.js 16 (App Router) - TypeScript, React 19
- **Backend:** Spring Boot 4.0.1 - Java 21, REST API
- **Database:** MySQL 8.x

---

## 2. High-Level Architecture

```mermaid
flowchart TB
    subgraph "👤 Client Layer"
        WEB["🌐 Next.js Web App\n(React 19, TypeScript)"]
    end

    subgraph "🔐 Authentication & State"
        AUTH["🔐 NextAuth.js\n(OAuth + Credentials)"]
        ZUSTAND["⚡ Zustand Store\n(Client State)"]
        TANSTACK["📡 TanStack Query\n(Server State)"]
    end

    subgraph "🌐 Next.js API Routes\n(Proxy / Server Actions)"
        API_PROXY["🔄 API Proxy Routes\n(/app/api/*)"]
    end

    subgraph "🔗 External Services"
        BACKEND["🎮 Spring Boot Backend\n(REST API)"]
        CLOUD["☁️ Cloudinary\n(File Storage)"]
    end

    subgraph "💾 Data Layer"
        MYSQL["🗄️ MySQL Database"]
    end

    WEB --> AUTH
    WEB --> ZUSTAND
    WEB --> TANSTACK
    WEB --> API_PROXY
    API_PROXY --> BACKEND
    API_PROXY --> CLOUD
    BACKEND --> MYSQL
```

---

## 3. Frontend Architecture (Next.js)

### 3.1 Directory Structure

```mermaid
flowchart TB
    subgraph "Next.js App Router"
        APP["📁 app/\n(Page Routes + API Routes)"]
        FEATURES["📁 features/\n(Domain-based modules)"]
        LIB["📁 lib/\n(Utilities, API)"]
        CONFIG["📁 config/\n(Environment)"]
        TYPES["📁 types/\n(TypeScript types)"]
        I18N["📁 i18n/\n(Internationalization)"]
    end

    APP --> FEATURES
    APP --> LIB
    APP --> CONFIG
    APP --> TYPES
    FEATURES --> LIB
    FEATURES --> TYPES
```

### 3.2 Page Routes

```mermaid
flowchart TB
    subgraph "👤 Public Routes"
        HOME["🏠 (main)/\nHomepage"]
        BOOKS["📚 (main)/books\nBook Listing"]
        BOOK_DETAIL["📖 (main)/detail/[slug]\nBook Detail"]
        CART["🛒 (main)/cart\nShopping Cart"]
        CHECKOUT["💳 (main)/checkout\nCheckout"]
        ORDERS["📦 (main)/orders\nMy Orders"]
    end

    subgraph "🔐 Auth Routes"
        LOGIN["🔑 (auth)/login\nLogin"]
        REGISTER["📝 (auth)/register\nRegister"]
        FORGOT_PWD["🔑 (auth)/forgot-password\nForgot Password"]
        VERIFY["✅ (auth)/verify/[id]\nVerify Email"]
        RESET_PWD["🔑 (auth)/reset-password\nReset Password"]
    end

    subgraph "🛍️ Seller Routes"
        SELLER_DASH["📊 seller/dashboard\nSeller Dashboard"]
        SELLER_BOOKS["📚 seller/dashboard/books\nManage Books"]
        SELLER_ORDERS["📦 seller/dashboard/orders\nManage Orders"]
        SELLER_RETURNS["📋 seller/dashboard/returns\nManage Returns"]
        SELLER_PROMOS["🏷️ seller/dashboard/promotions\nManage Promotions"]
    end

    subgraph "🏭 Warehouse Routes"
        WH_DASH["📊 warehouse/dashboard\nWarehouse Dashboard"]
        WH_IMPORT["📥 warehouse/dashboard/import\nImport Stock"]
        WH_BATCH["📦 warehouse/dashboard/batch\nManage Batches"]
        WH_DISPOSAL["🗑️ warehouse/dashboard/discard\nDisposal"]
    end

    subgraph "👑 Admin Routes"
        ADMIN_DASH["📊 admin/dashboard\nAdmin Dashboard"]
        ADMIN_ACCOUNTS["👥 admin/dashboard/accounts\nManage Accounts"]
        ADMIN_BOOKS["📚 admin/dashboard/books\nManage Books"]
        ADMIN_CATEGORIES["📂 admin/dashboard/categories\nManage Categories"]
        ADMIN_ORDERS["📦 admin/dashboard/orders\nManage Orders"]
        ADMIN_RETURNS["📋 admin/dashboard/returns\nManage Returns"]
        ADMIN_DISPOSAL["🗑️ admin/dashboard/return-to-warehouse\nReturn to Warehouse"]
    end

    subgraph "👤 User Routes"
        PROFILE["👤 profile\nUser Profile"]
        WISHLIST["❤️ (main)/wishlist\nWishlist"]
    end
```

### 3.3 API Routes (Frontend Proxy)

Frontend sử dụng Next.js API Routes làm proxy để gọi backend:

```mermaid
flowchart LR
    subgraph "Frontend Client"
        COMP["⚛️ React Components"]
    end

    subgraph "Next.js API Routes"
        PROXY["🔄 /app/api/*\n(Server-side Proxy)"]
    end

    subgraph "Spring Boot Backend"
        SPRING["🎮 REST API\n(25 Controllers)"]
    end

    COMP -->|"call API"| PROXY
    PROXY -->|"fetch + JWT"| SPRING
```

**API Endpoints mapping:**

| Frontend Route | Backend Controller |
|--------------|-------------------|
| `/api/auth/*` | AuthController |
| `/api/books/*` | BookController |
| `/api/orders/*` | OrderController |
| `/api/carts/*` | CartController |
| `/api/cart-items/*` | CartItemController |
| `/api/categories/*` | CategoryController |
| `/api/promotions/*` | PromotionController |
| `/api/users/*` | UserController |
| `/api/addresses/*` | AddressController |
| `/api/batches/*` | BatchController |
| `/api/import-stocks/*` | ImportStockController |
| `/api/stock-requests/*` | StockRequestController |
| `/api/purchase-orders/*` | PurchaseOrderController |
| `/api/return-requests/*` | ReturnRequestController |
| `/api/disposal-requests/*` | DisposalRequestController |
| `/api/variants/*` | VariantController |
| `/api/suppliers/*` | SupplierController |
| `/api/cloudinary/*` | CloudinaryController |

---

## 4. Backend Architecture (Spring Boot)

### 4.1 Layered Architecture

```mermaid
flowchart TB
    subgraph "Layer 1: Client"
        CLIENT["👤 Client\n(REST API Consumer)"]
    end

    subgraph "Layer 2: Controller"
        CTRL["🎮 Controllers\n(25 classes)\n- Handle HTTP\n- Validate input\n- Route to service"]
    end

    subgraph "Layer 3: Service"
        SVC["⚙️ Services\n(26 interfaces + 26 impls)\n- Business logic\n- Transaction management"]
    end

    subgraph "Layer 4: Repository"
        REPO["🗃️ Repositories\n(JPA Interfaces)\n- Data access\n- Query methods"]
    end

    subgraph "Layer 5: Database"
        DB["💾 MySQL\n(Entity tables)"]
    end

    CLIENT -->|"HTTP Request"| CTRL
    CTRL -->|"DTO"| SVC
    SVC -->|"Entity"| REPO
    REPO -->|"SQL"| DB
    DB -->|"Entity"| REPO
    REPO -->|"Entity"| SVC
    SVC -->|"DTO/Response"| CTRL
    CTRL -->|"HTTP Response"| CLIENT
```

### 4.2 Security Architecture

```mermaid
flowchart TB
    subgraph "Incoming Request"
        REQ["🌐 HTTP Request"]
    end

    subgraph "Security Filter Chain"
        CORS["🌐 CORS Config\n(DevCorsConfig / ProdCorsConfig)"]
        JWT["🔑 JwtAuthenticationFilter\n1. Extract Bearer token\n2. Validate JWT\n3. Set SecurityContext"]
        PERM["⚡ PermissionFilter\n1. Extract roles from context\n2. Match against Permission table\n3. Allow/Deny based on HttpMethod + PathPattern"]
    end

    subgraph "Controller"
        CTRL["🎮 Controller\n(@PreAuthorize checks)"]
    end

    subgraph "Database"
        USERS["👤 users"]
        ROLES["🏷️ roles"]
        PERMS["🔐 permissions"]
        USER_ROLES["🔗 user_roles"]
        ROLE_PERMS["🔗 role_permissions"]
    end

    REQ --> CORS
    CORS --> JWT
    JWT -->|"JWT Claims"| PERM
    PERM -->|"Allowed"| CTRL
    PERM -.->|"Denied: 403"| REQ
    CTRL -.->|"Invalid: 401/403"| REQ
```

---

## 5. Authentication Flow

```mermaid
sequenceDiagram
    participant U as 👤 User
    participant FE as 🌐 Next.js
    participant API as 🔄 API Routes
    participant BE as 🎮 Spring Boot
    participant DB as 💾 MySQL
    participant EMAIL as 📧 Email Service

    Note over U,EMAIL: Login Flow
    U->>FE: Enter email + password
    FE->>API: POST /api/auth/login
    API->>BE: POST /api/auth/login
    BE->>DB: Verify credentials
    DB-->>BE: User data
    BE-->>API: JWT + Refresh Token
    API-->>FE: Tokens
    FE->>FE: Store in Zustand + localStorage

    Note over U,EMAIL: Protected Route Access
    U->>FE: Access protected page
    FE->>API: GET /api/orders (with Bearer token)
    API->>BE: GET /api/orders (with Bearer token)
    BE->>BE: Validate JWT + Check permissions
    BE-->>API: Response data
    API-->>FE: JSON data
    FE-->>U: Render page

    Note over U,EMAIL: Token Refresh
    FE->>API: POST /api/auth/refresh
    API->>BE: POST /api/auth/refresh
    BE->>DB: Verify refresh token
    DB-->>BE: Token valid
    BE-->>API: New JWT
    API-->>FE: New JWT
```

---

## 6. Module Structure (Backend)

```mermaid
flowchart TB
    subgraph "vn.edu.iuh.fit.bookstorebackend"
        MODEL["📦 model/\n(31 Entities)"]
        CTRL["🎮 controller/\n(25 Controllers)"]
        SVC["⚙️ service/\n(26 Interfaces)"]
        SVC_IMPL["⚙️ service/Impl/\n(26 Implementations)"]
        REPO["🗄️ repository/\n(30 Repositories)"]
        DTO_REQ["📥 dto/request/\n(49 DTOs)"]
        DTO_RES["📤 dto/response/\n(32 DTOs)"]
        MAPPER["🔄 mapper/\n(22 Mappers)"]
        CONFIG["⚙️ config/\n(8 Configs)"]
        COMMON["📋 common/\n(11 Enums)"]
        EX["⚠️ exception/\n(Exception Handling)"]
    end
```

---

## 7. Tech Stack Summary

### Frontend
| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Next.js | 16.1.4 |
| Language | TypeScript | 5 |
| UI Library | Radix UI + Tailwind CSS | Latest |
| State (Client) | Zustand | 5.0.11 |
| State (Server) | TanStack Query | 5.90.21 |
| HTTP Client | Fetch API | Native |
| Validation | Zod + React Hook Form | 4.3.6 / 7.71.1 |
| Auth | NextAuth.js | 4.24.13 |
| i18n | i18next | 25.8.0 |
| Notifications | Sonner | 2.0.7 |

### Backend
| Component | Technology | Version |
|-----------|------------|---------|
| Framework | Spring Boot | 4.0.1 |
| Language | Java | 21 |
| Database | MySQL | 8.x |
| ORM | Spring Data JPA | (with Hibernate) |
| Security | Spring Security + JWT | OAuth2 Resource Server |
| JWT Library | java-jwt (Auth0) | 4.4.0 |
| Mapping | MapStruct | 1.5.5 |
| File Storage | Cloudinary | 1.38.0 |
| Email | Spring Mail | (built-in) |
| Build Tool | Maven | 3.x |

---

## 8. Domain Distribution

```mermaid
flowchart LR
    subgraph "Domain 1: Catalog"
        B["📚 Book"]
        C["📂 Category"]
        V["🏷️ Variant"]
        BV["📖 BookVariant"]
    end

    subgraph "Domain 2: Shopping"
        CA["🛒 Cart"]
        CI["📦 CartItem"]
        O["📋 Order"]
        OD["📄 OrderDetail"]
        A["🏠 Address"]
    end

    subgraph "Domain 3: Inventory"
        BA["📦 Batch"]
        IS["📥 ImportStock"]
        ISD["📄 ImportStockDetail"]
    end

    subgraph "Domain 4: Procurement"
        SR["📋 StockRequest"]
        PO["🛒 PurchaseOrder"]
        POI["📄 PurchaseOrderItem"]
    end

    subgraph "Domain 5: Returns"
        RR["📋 ReturnRequest"]
        RWR["📋 ReturnToWarehouseRequest"]
        DR["📋 DisposalRequest"]
        DRI["📄 DisposalRequestItem"]
    end

    subgraph "Domain 6: Marketing"
        P["🏷️ Promotion"]
        BN["🎨 Banner"]
        N["🔔 Notification"]
    end

    subgraph "Domain 7: Users"
        U["👤 User"]
        R["🏷️ Role"]
        P2["🔐 Permission"]
    end
```

---

## 9. Role-Based Access Control

```mermaid
flowchart TB
    subgraph "👤 User Roles"
        ADMIN["👑 Admin\nFull access"]
        SELLER["🛍️ Seller\nManage books, orders, promotions"]
        WAREHOUSE["🏭 Warehouse Staff\nManage inventory, batches, imports"]
        CUSTOMER["🛒 Customer\nBrowse, cart, orders"]
    end

    subgraph "🔐 Frontend Routes"
        ADMIN_ROUTES["admin/dashboard/*"]
        SELLER_ROUTES["seller/dashboard/*"]
        WAREHOUSE_ROUTES["warehouse/dashboard/*"]
        PUBLIC_ROUTES["(main)/*, (auth)/*"]
    end

    subgraph "🔐 Backend Permissions"
        ADMIN_PERMS["CREATE, READ, UPDATE, DELETE\nAll resources"]
        SELLER_PERMS["CREATE, READ, UPDATE\nBooks, Orders, Promotions, Returns"]
        WAREHOUSE_PERMS["CREATE, READ, UPDATE\nBatches, ImportStock, StockRequests, PurchaseOrders"]
        CUSTOMER_PERMS["READ\nBooks, Categories\nCREATE/UPDATE\nCart, Orders, Addresses"]
    end

    ADMIN --> ADMIN_ROUTES
    ADMIN --> ADMIN_PERMS
    SELLER --> SELLER_ROUTES
    SELLER --> SELLER_PERMS
    WAREHOUSE --> WAREHOUSE_ROUTES
    WAREHOUSE --> WAREHOUSE_PERMS
```

---

## 10. Data Flow - Full Stack

```mermaid
sequenceDiagram
    participant U as 👤 User
    participant NEXT as 🌐 Next.js App
    participant API as 🔄 API Route
    participant ZUS as ⚡ Zustand
    participant TQ as 📡 TanStack Query
    participant BE as 🎮 Spring Boot
    participant SVC as ⚙️ Service
    participant REPO as 🗄️ Repository
    participant DB as 💾 MySQL

    Note over U,DB: Read Flow (Book Listing)
    U->>NEXT: GET /books
    NEXT->>TQ: Fetch books (useQuery)
    TQ->>API: GET /api/books
    API->>BE: GET /api/books
    BE->>SVC: getAllBooks()
    SVC->>REPO: findAll()
    REPO->>DB: SELECT * FROM books
    DB-->>REPO: List<Book>
    REPO-->>SVC: List<Book>
    SVC-->>BE: PageResponse
    BE-->>API: JSON
    API-->>TQ: JSON
    TQ-->>NEXT: data
    NEXT-->>U: Render BookList

    Note over U,DB: Write Flow (Add to Cart)
    U->>NEXT: Click "Add to Cart"
    NEXT->>TQ: mutate (addToCart)
    TQ->>API: POST /api/carts/users/{id}/items
    API->>BE: POST /api/carts/users/{id}/items
    BE->>SVC: addToCart()
    SVC->>REPO: save(CartItem)
    REPO->>DB: INSERT cart_items
    DB-->>REPO: CartItem saved
    REPO-->>SVC: CartItem
    SVC-->>BE: CartResponse
    BE-->>API: JSON
    API-->>TQ: JSON
    TQ-->>NEXT: success
    NEXT->>ZUS: syncFromCart()
    ZUS-->>NEXT: cartItemCount updated
    NEXT-->>U: Toast "Added to cart!"
```

---

## 11. Deployment Overview

```mermaid
flowchart TB
    subgraph "Development"
        DEV_FE["💻 Next.js Dev\n(npm run dev)"]
        DEV_BE["💻 Spring Boot Dev\n(mvn spring-boot:run)"]
        DEV_DB["🗄️ Local MySQL\nor Docker"]
    end

    subgraph "Production"
        VERCEL["☁️ Vercel\n(Next.js Frontend)"]
        SERVER["🖥️ Server\n(Spring Boot JAR)"]
        PROD_DB["🗄️ MySQL\n(Hosted)"]
        CLOUDINARY["☁️ Cloudinary"]
        VNPAY_P["💳 VNPay\n(Production)"]
    end

    subgraph "Clients"
        USER["👤 End Users\n(Browsers)"]
    end

    USER --> VERCEL
    VERCEL --> SERVER
    SERVER --> PROD_DB
    SERVER --> CLOUDINARY
    SERVER --> VNPAY_P
```

---

## 12. Key Design Patterns

| Pattern | Backend Usage | Frontend Usage |
|---------|---------------|----------------|
| **Layered Architecture** | Controller → Service → Repository → Entity | Page → Hook → API Route → Backend |
| **DTO Pattern** | Request DTOs / Response DTOs + MapStruct | TypeScript interfaces |
| **Repository Pattern** | Spring Data JPA repositories | TanStack Query cache |
| **State Management** | - | Zustand (client state) + TanStack Query (server state) |
| **Strategy Pattern** | Payment methods (VNPay/COD) | - |
| **Observer Pattern** | Notification system | React hooks (useEffect) |
| **Feature-Based Organization** | - | `features/*/` folder structure |
| **Proxy Pattern** | - | Next.js API Routes as backend proxy |

---

*Generated by Senior BA Agent | BookStore Backend | 2026-04-25*
