package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.ReturnRequestStatus;

import java.time.LocalDateTime;

@Data
@JsonPropertyOrder({ "id", "orderId", "orderTotal", "reason", "responseNote", "status", "createdAt", "processedAt", "createdById", "createdByName", "processedById", "processedByName" })
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
}
