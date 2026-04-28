package vn.edu.iuh.fit.bookstorebackend.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBatchRequest {

    @NotNull(message = "Book ID is required")
    @Positive(message = "Book ID must be positive")
    private Long bookId;

    @NotNull(message = "Variant ID is required")
    @Positive(message = "Variant ID must be positive")
    private Long variantId;

    @Positive(message = "Import stock detail ID must be positive")
    private Long importStockDetailId;

    @NotNull(message = "Created by ID is required")
    @Positive(message = "Created by ID must be positive")
    private Long createdById;

    @NotNull(message = "Supplier ID is required")
    @Positive(message = "Supplier ID must be positive")
    private Long supplierId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;

    @NotNull(message = "Import price is required")
    @PositiveOrZero(message = "Import price cannot be negative")
    private Double importPrice;

    private LocalDate productionDate;
    private String batchCode;
}
