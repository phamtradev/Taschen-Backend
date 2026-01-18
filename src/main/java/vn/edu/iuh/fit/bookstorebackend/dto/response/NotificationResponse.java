package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonPropertyOrder({ "id", "title", "content", "createdAt", "isRead", "senderId", "senderName", "receiverId", "receiverName" })
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isRead;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
}
