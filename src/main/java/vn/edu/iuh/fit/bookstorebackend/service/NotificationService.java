package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.response.NotificationResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.User;

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
