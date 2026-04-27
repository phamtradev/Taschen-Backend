package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateImportStockRequest {

    @NotNull(message = "Supplier ID is required")
    @Positive(message = "Supplier ID must be positive")
    private Long supplierId;

    @NotNull(message = "Created by ID is required")
    @Positive(message = "Created by ID must be positive")
    private Long createdById;

    @NotNull(message = "Purchase order ID is required")
    @Positive(message = "Purchase order ID must be positive")
    private Long purchaseOrderId;

    @Valid
    private List<ImportStockDetailRequest> details;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportStockDetailRequest {
        @NotNull(message = "Book ID is required")
        @Positive(message = "Book ID must be positive")
        private Long bookId;

        @NotNull(message = "Variant ID is required")
        @Positive(message = "Variant ID must be positive")
        private Long variantId;

        @NotNull(message = "Supplier ID is required")
        @Positive(message = "Supplier ID must be positive")
        private Long supplierId;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        private int quantity;

        @NotNull(message = "Import price is required")
        @PositiveOrZero(message = "Import price cannot be negative")
        private double importPrice;
    }
}
