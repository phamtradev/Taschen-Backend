# BookStore Backend - Documentation Index

> **Phiên bản:** 1.0.0
> **Ngày:** 2026-04-25
> **Dự án:** BookStore E-commerce Backend
> **Framework:** Spring Boot 4.0.1 + Java 21

---

## Overview

Tài liệu này được generate tự động từ codebase bởi **Senior BA Agent**. Mỗi file tương ứng với một diagram hoặc tài liệu mô tả một phần của hệ thống.

---

## Folder Structure

```
docs/BA/
├── index.md                      # (file này) - Master index
├── architecture/                 # Sơ đồ kiến trúc hệ thống
├── class-diagram/              # Class diagram theo domain
├── sequence/                    # Sequence diagram (ứng với use case)
├── usecase/                     # Use case diagram + mô tả
├── er-diagram/                  # Entity-Relationship diagram
└── api-docs/                    # API documentation
```

---

## Architecture Documents

| File | Mô tả |
|------|--------|
| `architecture/arch-001-system-overview.md` | Tổng quan hệ thống, tech stack, kiến trúc 3-layer |
| `architecture/arch-002-domain-model.md` | Domain model overview, bounded contexts |

## Class Diagrams

| File | Domain | Entities |
|------|--------|----------|
| `class-diagram/class-001-catalog.md` | Catalog | Book, Category, Variant, BookVariant |
| `class-diagram/class-002-order.md` | Order | Order, OrderDetail, Cart, CartItem, Address |
| `class-diagram/class-003-user.md` | User | User, Role, Permission, Address |
| `class-diagram/class-004-inventory.md` | Inventory | Batch, BatchDetail, ImportStock, ImportStockDetail |
| `class-diagram/class-005-requests.md` | Requests | StockRequest, PurchaseOrder, PurchaseOrderItem |
| `class-diagram/class-006-returns.md` | Returns | ReturnRequest, ReturnToWarehouseRequest, DisposalRequest |
| `class-diagram/class-007-marketing.md` | Marketing | Promotion, Banner, Notification |
| `class-diagram/class-008-auth.md` | Auth | RefreshToken, VerificationToken |

## Use Cases

| File | UC # | Tên Use Case |
|------|------|-------------|
| `usecase/uc-001.md` | UC-001 | Browse Books |
| `usecase/uc-002.md` | UC-002 | Authentication (Register/Login) |
| `usecase/uc-003.md` | UC-003 | Shopping Cart |
| `usecase/uc-004.md` | UC-004 | Place Order |
| `usecase/uc-005.md` | UC-005 | Order Management |
| `usecase/uc-006.md` | UC-006 | Inventory Management |
| `usecase/uc-007.md` | UC-007 | Return & Disposal |
| `usecase/uc-008.md` | UC-008 | Promotion Management |
| `usecase/uc-009.md` | UC-009 | User Management |
| `usecase/uc-010.md` | UC-010 | Procurement |

## Sequence Diagrams

| File | Maps to | Mô tả |
|------|--------|--------|
| `sequence/seq-001.md` | UC-001 | Browse & Search Books |
| `sequence/seq-002.md` | UC-002 | Register & Login |
| `sequence/seq-003.md` | UC-003 | Add to Cart |
| `sequence/seq-004.md` | UC-004 | Checkout & Pay (VNPay) |
| `sequence/seq-005.md` | UC-005 | Order Fulfillment Flow |
| `sequence/seq-006.md` | UC-006 | Stock Replenishment |
| `sequence/seq-007.md` | UC-007 | Return Request Flow |
| `sequence/seq-008.md` | UC-008 | Disposal Request Flow |
| `sequence/seq-009.md` | UC-009 | Promotion Management |
| `sequence/seq-010.md` | UC-010 | Import Stock & Batch |

## State Machines

| File | Entity | Mô tả |
|------|--------|--------|
| `state/state-001-order.md` | Order | Order lifecycle |
| `state/state-002-purchase-order.md` | PurchaseOrder | Purchase order lifecycle |
| `state/state-003-stock-request.md` | StockRequest | Stock request lifecycle |
| `state/state-004-promotion.md` | Promotion | Promotion lifecycle |

## ER Diagram

| File | Mô tả |
|------|--------|
| `er-diagram/er-001-full.md` | Full ER Diagram của toàn bộ database |

## API Documentation

| File | Domain |
|------|--------|
| `api-docs/api-001-auth.md` | Authentication APIs |
| `api-docs/api-002-catalog.md` | Book, Category, Variant APIs |
| `api-docs/api-003-shopping.md` | Cart, CartItem APIs |
| `api-docs/api-004-order.md` | Order APIs |
| `api-docs/api-005-inventory.md` | Batch, ImportStock APIs |
| `api-docs/api-006-procurement.md` | PurchaseOrder, StockRequest APIs |
| `api-docs/api-007-returns.md` | Return, Disposal APIs |
| `api-docs/api-008-marketing.md` | Promotion, Banner APIs |
| `api-docs/api-009-user.md` | User, Role, Permission APIs |

---

## Actors Summary

| Actor | Mô tả | Roles |
|-------|--------|-------|
| **Guest** | Người dùng chưa đăng nhập | DUYỆT_SÁCH |
| **User** | Khách hàng đã đăng ký | MUA_HÀNG, THEO_DÕI_ĐƠN |
| **Seller** | Nhân viên bán hàng | QUẢN_LÝ_ĐƠN, TẠO_YC_NHẬP, KHUYẾN_MÃI |
| **Warehouse Staff** | Nhân viên kho | QUẢN_LÝ_KHO, NHẬP_HÀNG, DUYỆT_YC |
| **Admin** | Quản trị viên | TOÀN_QUYỀN |

---

## Business Domains

1. **Catalog Management** - Quản lý sách, thể loại, định dạng
2. **Customer Shopping** - Giỏ hàng, đặt hàng, theo dõi
3. **Promotions** - Khuyến mãi, mã giảm giá
4. **User Management** - Tài khoản, vai trò, phân quyền
5. **Procurement** - Đặt hàng nhà cung cấp
6. **Warehouse & Inventory** - Nhập kho, lô hàng, tồn kho
7. **Returns & Disposals** - Trả hàng, thanh lý
8. **Payments** - Thanh toán VNPay, COD
9. **Notifications** - Thông báo người dùng

---

## Status Flows

### Order Lifecycle
```
UNPAID → PENDING → PROCESSING → DELIVERING → COMPLETED
   ↓         ↓           ↓             ↓
CANCELLED CANCELLED  CANCELLED   CANCELLED/RETURNED
```

### Purchase Order Lifecycle
```
PENDING → APPROVED → ORDERED → (nhập kho) → COMPLETED
   ↓          ↓
REJECTED  CANCELLED
```

### Promotion Lifecycle
```
PENDING → ACTIVE → PAUSED
   ↓         ↓
REJECTED  (resume) → ACTIVE
```

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 4.0.1 |
| Language | Java 21 |
| Database | MySQL |
| ORM | Spring Data JPA |
| Auth | JWT (java-jwt 4.4.0) |
| Mapping | MapStruct 1.5.5 |
| File Storage | Cloudinary 1.38.0 |
| Email | Spring Mail |
| Build | Maven |

---

## Security Model

- **JWT Authentication** - Token-based auth với access + refresh tokens
- **Role-Based Access Control (RBAC)** - 5 roles: GUEST, USER, ADMIN, SELLER, WAREHOUSE_STAFF
- **Permission-Based Authorization** - Fine-grained permissions (HttpMethod + pathPattern)
- **PermissionFilter** - Intercepts every authenticated request to validate permissions

---

*Generated by Senior BA Agent | BookStore Backend | 2026-04-25*
