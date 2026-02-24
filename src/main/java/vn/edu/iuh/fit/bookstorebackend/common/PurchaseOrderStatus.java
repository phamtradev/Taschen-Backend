package vn.edu.iuh.fit.bookstorebackend.common;

public enum PurchaseOrderStatus {
    PENDING,
    APPROVED,
    REJECTED,
    ORDERED,
    CANCELLED // đã hủy sau khi approve do có vấn đề khi nhập hàng (importStock)
}
