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
import vn.edu.iuh.fit.bookstorebackend.mapper.NotificationMapper;
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
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() throws IdInvalidException {
        User currentUser = getCurrentUser();
        List<Notification> notifications = notificationRepository.findByReceiverOrderByCreatedAtDesc(currentUser);
        return mapToNotificationResponseList(notifications);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) throws IdInvalidException {
        validateNotificationId(notificationId);
        
        User currentUser = getCurrentUser();
        Notification notification = findNotificationById(notificationId);
        validateNotificationAccess(notification, currentUser);
        
        markNotificationAsRead(notification);
        Notification updatedNotification = notificationRepository.save(notification);
        
        return notificationMapper.toNotificationResponse(updatedNotification);
    }
    
    private void validateNotificationId(Long notificationId) throws IdInvalidException {
        if (notificationId == null || notificationId <= 0) {
            throw new IdInvalidException("Notification identifier is invalid: " + notificationId);
        }
    }
    
    private Notification findNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with identifier: " + notificationId));
    }
    
    private void validateNotificationAccess(Notification notification, User currentUser) {
        if (!notification.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You do not have permission to access this notification");
        }
    }
    
    private void markNotificationAsRead(Notification notification) {
        notification.setRead(true);
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
        Notification notification = createNotificationFromParams(sender, receiver, title, content);
        notificationRepository.save(notification);
    }
    
    private Notification createNotificationFromParams(User sender, User receiver, String title, String content) {
        Notification notification = new Notification();
        notification.setSender(sender);
        notification.setReceiver(receiver);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        return notification;
    }

    private User getCurrentUser() throws IdInvalidException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        validateAuthentication(auth);
        
        String email = extractEmailFromAuth(auth);
        User user = findUserByEmail(email);
        validateUserIsActive(user);
        
        return user;
    }
    
    private void validateAuthentication(Authentication auth) {
        if (auth == null) {
            throw new RuntimeException("Authentication context is null. Please login first.");
        }
        if (!auth.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated. Please login first.");
        }
        if (auth instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("User is not authenticated. Please login first.");
        }
    }
    
    private String extractEmailFromAuth(Authentication auth) {
        String email = auth.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("User email is not found in authentication context.");
        }
        return email;
    }
    
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    private void validateUserIsActive(User user) {
        if (!user.isActive()) {
            throw new RuntimeException("User account is inactive. Please contact administrator.");
        }
    }

    private List<NotificationResponse> mapToNotificationResponseList(List<Notification> notifications) {
        return notifications.stream()
                .map(notificationMapper::toNotificationResponse)
                .collect(Collectors.toList());
    }
}
