#!/usr/bin/env python3
"""
Refactor Taschen-Backend: Layered Architecture -> Package by Feature
Run from project root: python refactor.py
"""

import os
import re
import shutil
from pathlib import Path

PROJECT_ROOT = Path("d:/Taschen-Backend")
JAVA_ROOT = PROJECT_ROOT / "src/main/java/vn/edu/iuh/fit/bookstorebackend"
BASE_PKG = "vn.edu.iuh.fit.bookstorebackend"

# ── Class → domain mapping ──────────────────────────────────────────────────

CLASS_TO_DOMAIN = {
    # BOOK
    "Book": "book", "BookVariant": "book", "BookEmbedding": "book",
    "Variant": "book", "Category": "book",
    "BookRepository": "book", "BookVariantRepository": "book",
    "BookEmbeddingRepository": "book", "VariantRepository": "book",
    "CategoryRepository": "book",
    "BookService": "book", "BookServiceImpl": "book",
    "BookVariantService": "book", "BookVariantServiceImpl": "book",
    "BookEmbeddingService": "book", "BookEmbeddingServiceImpl": "book",
    "CategoryService": "book", "CategoryServiceImpl": "book",
    "VariantService": "book", "VariantServiceImpl": "book",
    "BookController": "book", "BookVariantController": "book",
    "CategoryController": "book", "VariantController": "book",
    "BookMapper": "book", "BookVariantMapper": "book",
    "CategoryMapper": "book", "VariantMapper": "book",
    "CreateBookRequest": "book", "UpdateBookRequest": "book",
    "CreateBookVariantRequest": "book", "UpdateBookVariantRequest": "book",
    "CreateCategoryRequest": "book", "UpdateCategoryRequest": "book",
    "CreateVariantRequest": "book", "UpdateVariantRequest": "book",
    "BookResponse": "book", "BookVariantResponse": "book",
    "CategoryResponse": "book", "VariantResponse": "book",

    # INVENTORY
    "Batch": "inventory", "BatchDetail": "inventory",
    "ImportStock": "inventory", "ImportStockDetail": "inventory",
    "StockRequest": "inventory", "DisposalRequest": "inventory",
    "DisposalRequestItem": "inventory",
    "BatchRepository": "inventory", "BatchDetailRepository": "inventory",
    "ImportStockRepository": "inventory", "ImportStockDetailRepository": "inventory",
    "StockRequestRepository": "inventory", "DisposalRequestRepository": "inventory",
    "DisposalRequestItemRepository": "inventory",
    "BatchService": "inventory", "BatchServiceImpl": "inventory",
    "ImportStockService": "inventory", "ImportStockServiceImpl": "inventory",
    "StockRequestService": "inventory", "StockRequestServiceImpl": "inventory",
    "DisposalRequestService": "inventory", "DisposalRequestServiceImpl": "inventory",
    "BatchController": "inventory", "ImportStockController": "inventory",
    "StockRequestController": "inventory", "DisposalRequestController": "inventory",
    "BatchMapper": "inventory", "ImportStockMapper": "inventory",
    "StockRequestMapper": "inventory", "DisposalRequestMapper": "inventory",
    "CreateBatchRequest": "inventory", "CreateBatchDetailRequest": "inventory",
    "CreateImportStockRequest": "inventory", "CreateStockRequestRequest": "inventory",
    "ApproveStockRequestRequest": "inventory", "RejectStockRequestRequest": "inventory",
    "CreateDisposalRequestRequest": "inventory", "ProcessDisposalRequestRequest": "inventory",
    "UpdateQuantityRequest": "inventory",
    "BatchResponse": "inventory", "BatchDetailResponse": "inventory",
    "ImportStockResponse": "inventory", "ImportStockDetailResponse": "inventory",
    "StockRequestResponse": "inventory", "DisposalRequestResponse": "inventory",
    "DisposalRequestItemResponse": "inventory", "ReceiveStockResponse": "inventory",

    # ORDER
    "Order": "order", "OrderDetail": "order",
    "ReturnRequest": "order", "ReturnToWarehouseRequest": "order",
    "OrderRepository": "order", "OrderDetailRepository": "order",
    "ReturnRequestRepository": "order", "ReturnToWarehouseRequestRepository": "order",
    "OrderService": "order", "OrderServiceImpl": "order",
    "ReturnRequestService": "order", "ReturnRequestServiceImpl": "order",
    "ReturnToWarehouseRequestService": "order", "ReturnToWarehouseRequestServiceImpl": "order",
    "OrderController": "order", "ReturnRequestController": "order",
    "ReturnToWarehouseRequestController": "order",
    "OrderMapper": "order", "ReturnRequestMapper": "order",
    "ReturnToWarehouseRequestMapper": "order",
    "CreateOrderRequest": "order", "UpdateOrderStatusRequest": "order",
    "UpdatePaymentMethodRequest": "order",
    "CreateReturnRequestRequest": "order", "ProcessReturnRequestRequest": "order",
    "CreateReturnToWarehouseRequestRequest": "order",
    "ProcessReturnToWarehouseRequestRequest": "order",
    "OrderResponse": "order", "OrderDetailResponse": "order",
    "ReturnRequestResponse": "order", "ReturnToWarehouseRequestResponse": "order",

    # CART
    "Cart": "cart", "CartItem": "cart",
    "CartRepository": "cart", "CartItemRepository": "cart",
    "CartService": "cart", "CartServiceImpl": "cart",
    "CartItemService": "cart", "CartItemServiceImpl": "cart",
    "CartController": "cart", "CartItemController": "cart",
    "CartMapper": "cart", "CartItemMapper": "cart",
    "AddToCartRequest": "cart", "UpdateCartItemRequest": "cart",
    "CartResponse": "cart", "CartItemResponse": "cart",

    # USER
    "User": "user", "Address": "user", "Role": "user", "Permission": "user",
    "UserRepository": "user", "AddressRepository": "user",
    "RoleRepository": "user", "PermissionRepository": "user",
    "UserService": "user", "UserServiceImpl": "user",
    "AddressService": "user", "AddressServiceImpl": "user",
    "RoleService": "user", "RoleServiceImpl": "user",
    "PermissionService": "user", "PermissionServiceImpl": "user",
    "UserController": "user", "AddressController": "user",
    "RoleController": "user", "PermissionController": "user",
    "UserMapper": "user", "AddressMapper": "user",
    "RoleMapper": "user", "PermissionMapper": "user",
    "CreateUserRequest": "user", "UpdateUserRequest": "user",
    "AddressRequest": "user", "CreateRoleRequest": "user",
    "CreatePermissionRequest": "user", "CreatePermissionForRoleRequest": "user",
    "SetUserRoleCodesRequest": "user", "ChangePasswordRequest": "user",
    "RegisterRequest": "user",
    "UserResponse": "user", "AddressResponse": "user",
    "RoleResponse": "user", "PermissionResponse": "user", "RegisterResponse": "user",

    # AUTH
    "RefreshToken": "auth", "VerificationToken": "auth",
    "RefreshTokenRepository": "auth", "VerificationTokenRepository": "auth",
    "AuthService": "auth", "AuthServiceImpl": "auth",
    "AuthController": "auth",
    "AuthenticationRequest": "auth", "RefreshTokenRequest": "auth",
    "ForgotPasswordRequest": "auth", "ResetPasswordRequest": "auth",
    "VerifyTokenRequest": "auth",
    "AuthenticationResponse": "auth", "RefreshTokenResponse": "auth",

    # SUPPLIER
    "Supplier": "supplier", "PurchaseOrder": "supplier", "PurchaseOrderItem": "supplier",
    "SupplierRepository": "supplier", "PurchaseOrderRepository": "supplier",
    "PurchaseOrderItemRepository": "supplier",
    "SupplierService": "supplier", "SupplierServiceImpl": "supplier",
    "PurchaseOrderService": "supplier", "PurchaseOrderServiceImpl": "supplier",
    "SupplierController": "supplier", "PurchaseOrderController": "supplier",
    "SupplierMapper": "supplier", "PurchaseOrderMapper": "supplier",
    "CreateSupplierRequest": "supplier", "UpdateSupplierRequest": "supplier",
    "CreatePurchaseOrderRequest": "supplier",
    "CreatePurchaseOrderFromStockRequestRequest": "supplier",
    "ApprovePurchaseOrderRequest": "supplier", "CancelPurchaseOrderRequest": "supplier",
    "PayPurchaseOrderRequest": "supplier",
    "SupplierResponse": "supplier", "PurchaseOrderResponse": "supplier",
    "PurchaseOrderItemResponse": "supplier",

    # MARKETING
    "Promotion": "marketing", "Banner": "marketing",
    "PromotionRepository": "marketing", "BannerRepository": "marketing",
    "PromotionService": "marketing", "PromotionServiceImpl": "marketing",
    "BannerService": "marketing", "BannerServiceImpl": "marketing",
    "PromotionController": "marketing", "BannerController": "marketing",
    "PromotionMapper": "marketing", "BannerMapper": "marketing",
    "CreatePromotionRequest": "marketing", "BannerRequest": "marketing",
    "PromotionResponse": "marketing", "BannerResponse": "marketing",

    # PAYMENT
    "VnPayService": "payment", "VnPayServiceImpl": "payment",
    "PaymentController": "payment",
    "VnPayConfig": "payment", "VnPayUtil": "payment",

    # NOTIFICATION
    "Notification": "notification",
    "NotificationRepository": "notification",
    "NotificationService": "notification", "NotificationServiceImpl": "notification",
    "NotificationController": "notification",
    "NotificationMapper": "notification",
    "NotificationResponse": "notification",

    # SHARED — config, exception, util, common, cloudinary
    "AsyncConfig": "shared", "CloudinaryConfig": "shared",
    "DevCorsConfig": "shared", "ProdCorsConfig": "shared",
    "SecurityConfiguration": "shared", "JwtAuthenticationFilter": "shared",
    "PermissionFilter": "shared",
    "GlobalExceptionHandler": "shared", "IdInvalidException": "shared",
    "FormatRestRespone": "shared", "JwtService": "shared",
    "MailService": "shared", "PaginationUtil": "shared",
    "RestRespone": "shared", "PageResponse": "shared",
    "AddressType": "shared", "DisposalRequestStatus": "shared",
    "Gender": "shared", "HttpMethod": "shared", "OrderStatus": "shared",
    "PaymentMethod": "shared", "PromotionStatus": "shared",
    "PurchaseOrderStatus": "shared", "ReturnRequestStatus": "shared",
    "ReturnToWarehouseRequestStatus": "shared", "StockRequestStatus": "shared",
    "CloudinaryService": "shared", "CloudinaryServiceImpl": "shared",
    "CloudinaryController": "shared",
}

# Files that stay at root, never moved
SKIP_FILES = {"BookstorebackendApplication"}


def resolve_new_sub_path(old_rel: str, class_name: str, domain: str) -> str:
    """Map old relative path -> new relative path under the new domain layout."""
    p = old_rel.replace("\\", "/")

    # Special cases for payment utilities
    if class_name in ("VnPayConfig",):
        return f"payment/config/{class_name}.java"
    if class_name in ("VnPayUtil",):
        return f"payment/util/{class_name}.java"

    if p.startswith("controller/"):
        sub = "controller"
    elif p.startswith("service/Impl/"):
        sub = "service/impl"
    elif p.startswith("service/"):
        sub = "service"
    elif p.startswith("repository/"):
        sub = "repository"
    elif p.startswith("mapper/"):
        sub = "mapper"
    elif p.startswith("model/"):
        sub = "model"
    elif p.startswith("dto/request/"):
        sub = "dto/request"
    elif p.startswith("dto/response/"):
        sub = "dto/response"
    elif p.startswith("config/"):
        sub = "config"
    elif p.startswith("exception/"):
        sub = "exception"
    elif p.startswith("util/"):
        sub = "util"
    elif p.startswith("common/"):
        sub = "common"
    else:
        return p  # already at root

    return f"{domain}/{sub}/{class_name}.java"


def path_to_package(rel_path: str) -> str:
    """Convert relative file path to Java package name."""
    p = rel_path.replace("\\", "/")
    dir_part = "/".join(p.split("/")[:-1])
    if not dir_part:
        return BASE_PKG
    return f"{BASE_PKG}.{dir_part.replace('/', '.')}"


def main():
    print("=" * 60)
    print("Taschen-Backend: Layered → Package by Feature")
    print("=" * 60)

    # ── Phase 1: collect all .java files ──────────────────────────
    all_files = {f.stem: f for f in JAVA_ROOT.rglob("*.java")}
    print(f"\nFound {len(all_files)} Java files\n")

    # ── Phase 2: move files + update package declarations ─────────
    print("Phase 1/3 — Moving files & updating package declarations...")
    import_map: dict[str, str] = {}   # old FQN → new FQN
    moved = 0
    skipped = 0

    for class_name, old_path in sorted(all_files.items()):
        if class_name in SKIP_FILES:
            skipped += 1
            continue

        if class_name not in CLASS_TO_DOMAIN:
            print(f"  [WARN] No domain mapping for: {class_name} — skipped")
            skipped += 1
            continue

        domain = CLASS_TO_DOMAIN[class_name]
        old_rel = str(old_path.relative_to(JAVA_ROOT)).replace("\\", "/")
        new_sub = resolve_new_sub_path(old_rel, class_name, domain)
        new_path = JAVA_ROOT / new_sub

        old_pkg = path_to_package(old_rel)
        new_pkg = path_to_package(new_sub)

        # Record FQN mapping for import-update pass
        import_map[f"{old_pkg}.{class_name}"] = f"{new_pkg}.{class_name}"

        if old_path.resolve() == new_path.resolve():
            continue  # already in correct place

        # Create target directory
        new_path.parent.mkdir(parents=True, exist_ok=True)

        # Read, update package declaration, write
        content = old_path.read_text(encoding="utf-8")
        content = re.sub(
            r"^package\s+" + re.escape(old_pkg) + r"\s*;",
            f"package {new_pkg};",
            content,
            count=1,
            flags=re.MULTILINE,
        )
        new_path.write_text(content, encoding="utf-8")
        old_path.unlink()

        moved += 1
        print(f"  ✓  {old_rel}")
        print(f"     → {new_sub}")

    print(f"\n  Moved: {moved}  |  Skipped: {skipped}")

    # ── Phase 3: update imports across all remaining .java files ──
    print("\nPhase 2/3 — Updating imports across all files...")
    updated_files = 0

    for java_file in JAVA_ROOT.rglob("*.java"):
        try:
            content = java_file.read_text(encoding="utf-8")
        except Exception as e:
            print(f"  [ERR] Cannot read {java_file}: {e}")
            continue

        original = content
        for old_fqn, new_fqn in import_map.items():
            content = content.replace(f"import {old_fqn};", f"import {new_fqn};")

        if content != original:
            java_file.write_text(content, encoding="utf-8")
            updated_files += 1

    print(f"  Updated imports in {updated_files} files")

    # ── Phase 4: clean up empty directories ───────────────────────
    print("\nPhase 3/3 — Cleaning up empty directories...")
    _remove_empty_dirs(JAVA_ROOT)

    print("\n" + "=" * 60)
    print("Refactoring complete!")
    print("Next step: run  mvn compile  to verify there are no errors.")
    print("=" * 60)


def _remove_empty_dirs(path: Path):
    for child in list(path.iterdir()):
        if child.is_dir():
            _remove_empty_dirs(child)
            try:
                child.rmdir()
            except OSError:
                pass  # not empty


if __name__ == "__main__":
    main()
