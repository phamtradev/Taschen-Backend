package vn.edu.iuh.fit.bookstorebackend.inventory.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.iuh.fit.bookstorebackend.shared.common.DisposalRequestStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "id", "reason", "responseNote", "status", "createdAt", "processedAt", "createdBy", "processedBy", "items" })
public class DisposalRequestResponse {
    private Long id;
    private String reason;
    private String responseNote;
    private DisposalRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private UserInfo createdBy;
    private UserInfo processedBy;
    private List<DisposalRequestItemResponse> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String email;
    }
}
