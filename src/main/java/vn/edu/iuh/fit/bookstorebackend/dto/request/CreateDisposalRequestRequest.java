package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDisposalRequestRequest {
    private String reason;
    private List<DisposalItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisposalItemRequest {
        private Long batchId;
        private int quantity;
    }
}
