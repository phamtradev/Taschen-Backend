package vn.edu.iuh.fit.bookstorebackend.common;

public enum OrderStatus {
    UNPAID,      // Chưa thanh toán
    PENDING,     // Chờ xác nhận
    PROCESSING,  // Đang xử lý
    DELIVERING,  // Đang giao
    COMPLETED,   // Đã giao thành công
    CANCELLED,   // Đã hủy
    RETURNED     // Đã trả lại
}
