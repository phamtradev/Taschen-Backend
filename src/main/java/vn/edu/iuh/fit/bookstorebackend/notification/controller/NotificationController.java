package vn.edu.iuh.fit.bookstorebackend.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.notification.dto.response.NotificationResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.dto.WsEvent;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.notification.service.NotificationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    /** Test endpoint: POST /api/notifications/test/{userId}
     *  Sends a test WsEvent directly to verify WebSocket pipeline.
     *  Example: POST http://localhost:8080/api/notifications/test/5
     */
    @PostMapping("/test/{userId}")
    public ResponseEntity<Map<String, String>> sendTestNotification(
            @PathVariable Long userId) {
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + userId,
                new WsEvent("CREATED", "NOTIFICATION", -1L, "test"));
        Map<String, String> resp = new HashMap<>();
        resp.put("message", "Test WsEvent sent to /topic/notifications/" + userId);
        return ResponseEntity.ok(resp);
    }

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

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId) throws IdInvalidException {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications() throws IdInvalidException {
        notificationService.deleteAllNotifications();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
