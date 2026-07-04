package vn.edu.iuh.fit.bookstorebackend.book.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookVariantRequest {
    @PositiveOrZero(message = "Price must be zero or positive")
    private Double price;

    @PositiveOrZero(message = "Stock quantity must be zero or positive")
    private Integer stockQuantity;
}
