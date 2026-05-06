package vn.edu.iuh.fit.bookstorebackend.shared.common;

public enum OrderStatus {
    PENDING_PAYMENT, // Chờ thanh toán
    PENDING,         // Chờ xác nhận
    PROCESSING,      // Đang xử lý
    DELIVERING,      // Đang giao
    COMPLETED,       // Đã giao thành công
    CANCELLED,       // Đã hủy
    RETURNED         // Đã trả lại
}
