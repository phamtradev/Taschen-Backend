package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.bookstorebackend.common.DisposalRequestStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "id", "reason", "responseNote", "status", "createdAt", "processedAt", "createdById", "createdByName", "processedById", "processedByName", "items" })
public class DisposalRequestResponse {
    private Long id;
    private String reason;
    private String responseNote;
    private DisposalRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private Long createdById;
    private String createdByName;
    private Long processedById;
    private String processedByName;
    private List<DisposalRequestItemResponse> items;
}
