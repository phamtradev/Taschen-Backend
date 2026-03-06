package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "id", "batchId", "quantity", "remainingQuantityAfter", "batch" })
public class DisposalRequestItemResponse {
    private Long id;
    private int quantity;
    private Integer remainingQuantityAfter;
    private Long batchId;
    private BatchResponse batch;
}
