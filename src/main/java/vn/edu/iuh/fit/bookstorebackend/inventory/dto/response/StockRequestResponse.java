package vn.edu.iuh.fit.bookstorebackend.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.shared.common.StockRequestStatus;

import java.time.LocalDateTime;

@Data
@JsonPropertyOrder({ "id", "quantity", "reason", "status", "createdAt", "processedAt", "responseMessage", "bookId", "bookTitle", "variantId", "variantName", "createdById", "createdByName", "processedById", "processedByName" })
public class StockRequestResponse {

    private Long id;
    private int quantity;
    private String reason;
    private StockRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String responseMessage;
    private Long bookId;
    private String bookTitle;
    private Long variantId;
    private String variantName;
    private Long createdById;
    private String createdByName;
    private Long processedById;
    private String processedByName;
}
