package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.StockRequestStatus;

import java.time.LocalDateTime;

@Data
@JsonPropertyOrder({ "id", "quantity", "reason", "status", "createdAt", "processedAt", "responseMessage", "bookId", "bookTitle", "createdById", "createdByName", "processedById", "processedByName" })
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
    private Long createdById;
    private String createdByName;
    private Long processedById;
    private String processedByName;
}
