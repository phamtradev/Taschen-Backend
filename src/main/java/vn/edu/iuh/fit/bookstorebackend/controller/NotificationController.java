package vn.edu.iuh.fit.bookstorebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.response.NotificationResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.NotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() 
            throws IdInvalidException {
        List<NotificationResponse> notifications = 
                notificationService.getMyNotifications();
        return ResponseEntity.status(HttpStatus.OK).body(notifications);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId) throws IdInvalidException {
        NotificationResponse notificationResponse = 
                notificationService.markAsRead(notificationId);
        return ResponseEntity.status(HttpStatus.OK).body(notificationResponse);
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead() 
            throws IdInvalidException {
        notificationService.markAllAsRead();
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() 
            throws IdInvalidException {
        long count = notificationService.getUnreadCount();
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
