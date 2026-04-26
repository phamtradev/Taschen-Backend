package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderFromStockRequestRequest {

    @NotNull(message = "Stock request ID is required")
    @Positive(message = "Stock request ID must be positive")
    private Long stockRequestId;

    @NotNull(message = "Supplier ID is required")
    @Positive(message = "Supplier ID must be positive")
    private Long supplierId;

    @NotNull(message = "Created by ID is required")
    @Positive(message = "Created by ID must be positive")
    private Long createdById;

    @NotNull(message = "Import price is required")
    @PositiveOrZero(message = "Import price cannot be negative")
    private double importPrice;

    private String note;
}
