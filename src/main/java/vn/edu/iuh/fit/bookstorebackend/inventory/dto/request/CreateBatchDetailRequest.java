package vn.edu.iuh.fit.bookstorebackend.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBatchDetailRequest {

    @NotNull(message = "Batch ID is required")
    @Positive(message = "Batch ID must be positive")
    private Long batchId;

    @NotNull(message = "Order detail ID is required")
    @Positive(message = "Order detail ID must be positive")
    private Long orderDetailId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private int quantity;
}
