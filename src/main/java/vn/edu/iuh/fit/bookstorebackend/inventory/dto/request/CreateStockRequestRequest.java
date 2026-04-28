package vn.edu.iuh.fit.bookstorebackend.inventory.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockRequestRequest {

    @NotNull(message = "Book ID is required")
    @Positive(message = "Book ID must be positive")
    private Long bookId;

    @Positive(message = "Variant ID must be positive")
    private Long variantId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private int quantity;

    private String reason;

    @NotNull(message = "Created by ID is required")
    @Positive(message = "Created by ID must be positive")
    private Long createdById;
}
