package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.ReturnToWarehouseRequestStatus;

import java.time.LocalDateTime;

@Data
@JsonPropertyOrder({ "id", "bookId", "bookTitle", "quantity", "reason", "responseNote", "status", "createdAt", "processedAt", "createdById", "createdByName", "processedById", "processedByName" })
public class ReturnToWarehouseRequestResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private int quantity;
    private String reason;
    private String responseNote;
    private ReturnToWarehouseRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private Long createdById;
    private String createdByName;
    private Long processedById;
    private String processedByName;
}
