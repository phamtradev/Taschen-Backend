package vn.edu.iuh.fit.bookstorebackend.notification.service;

import vn.edu.iuh.fit.bookstorebackend.notification.dto.response.NotificationResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.user.model.User;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getMyNotifications() throws IdInvalidException;

    NotificationResponse markAsRead(Long notificationId) throws IdInvalidException;

    void markAllAsRead() throws IdInvalidException;

    long getUnreadCount() throws IdInvalidException;

    /**
     * Phương thức nội bộ để hệ thống tạo thông báo.
     * Ví dụ: Gửi thông báo khi đơn hàng được giao.
     */
    void createNotification(User sender, User receiver, String title, String content);
}
