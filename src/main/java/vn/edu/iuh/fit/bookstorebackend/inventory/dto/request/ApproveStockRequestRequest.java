package vn.edu.iuh.fit.bookstorebackend.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveStockRequestRequest {

    @NotNull(message = "Processed by ID is required")
    @Positive(message = "Processed by ID must be positive")
    private Long processedById;

    private String responseMessage;
}
