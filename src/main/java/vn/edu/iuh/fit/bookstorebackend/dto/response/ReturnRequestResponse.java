package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.ReturnRequestStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonPropertyOrder({ "id", "orderId", "orderTotal", "reason", "responseNote", "status", "createdAt", "processedAt", "createdById", "createdByName", "processedById", "processedByName", "items" })
public class ReturnRequestResponse {

    private Long id;
    private Long orderId;
    private Double orderTotal;
    private String reason;
    private String responseNote;
    private ReturnRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private Long createdById;
    private String createdByName;
    private Long processedById;
    private String processedByName;
    private List<OrderItemInfo> items;

    @Data
    public static class OrderItemInfo {
        private Long bookId;
        private String bookTitle;
        private String bookAuthor;
        private int quantity;
        private double unitPrice;
        private double totalPrice;
    }
}
