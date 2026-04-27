package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBookVariantRequest {
    @NotNull(message = "Book ID is required")
    @Positive(message = "Book ID must be positive")
    private Long bookId;

    @NotNull(message = "Variant ID is required")
    @Positive(message = "Variant ID must be positive")
    private Long variantId;

    @PositiveOrZero(message = "Price cannot be negative")
    private Double price;

    private Integer stockQuantity;
}
