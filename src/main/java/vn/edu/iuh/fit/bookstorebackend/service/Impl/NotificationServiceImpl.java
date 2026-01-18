package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.response.NotificationResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.Notification;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.repository.NotificationRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() throws IdInvalidException {
        User currentUser = getCurrentUser();
        List<Notification> notifications = notificationRepository.findByReceiverOrderByCreatedAtDesc(currentUser);
        return notifications.stream()
                .map(this::convertToNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) throws IdInvalidException {
        if (notificationId == null || notificationId <= 0) {
            throw new IdInvalidException("Notification identifier is invalid: " + notificationId);
        }

        User currentUser = getCurrentUser();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with identifier: " + notificationId));

        if (!notification.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You do not have permission to access this notification");
        }

        notification.setRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        return convertToNotificationResponse(updatedNotification);
    }

    @Override
    @Transactional
    public void markAllAsRead() throws IdInvalidException {
        User currentUser = getCurrentUser();
        notificationRepository.markAllAsReadByReceiver(currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() throws IdInvalidException {
        User currentUser = getCurrentUser();
        return notificationRepository.countByReceiverAndIsRead(currentUser, false);
    }

    @Override
    @Transactional
    public void createNotification(User sender, User receiver, String title, String content) {
        Notification notification = new Notification();
        notification.setSender(sender); // 'sender' có thể là null nếu là thông báo hệ thống
        notification.setReceiver(receiver);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    private User getCurrentUser() throws IdInvalidException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Kiểm tra authentication
        if (auth == null) {
            throw new RuntimeException("Authentication context is null. Please login first.");
        }
        
        if (!auth.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated. Please login first.");
        }
        
        if (auth instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("User is not authenticated. Please login first.");
        }

        // Lấy email từ authentication
        String email = auth.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("User email is not found in authentication context.");
        }

        // Tìm user trong database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Kiểm tra user có active không
        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive. Please contact administrator.");
        }

        return user;
    }

    private NotificationResponse convertToNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitle(notification.getTitle());
        response.setContent(notification.getContent());
        response.setCreatedAt(notification.getCreatedAt());
        response.setIsRead(notification.isRead());
        response.setReceiverId(notification.getReceiver().getId());
        response.setReceiverName(getUserDisplayName(notification.getReceiver()));

        if (notification.getSender() != null) {
            response.setSenderId(notification.getSender().getId());
            response.setSenderName(getUserDisplayName(notification.getSender()));
        }

        return response;
    }

    private String getUserDisplayName(User user) {
        if (user.getFirstName() != null && user.getLastName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            return user.getFirstName();
        } else if (user.getLastName() != null) {
            return user.getLastName();
        } else {
            return user.getEmail();
        }
    }
}
