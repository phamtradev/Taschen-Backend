package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreatePromotionRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Code is required")
    @Size(max = 50, message = "Code must not exceed 50 characters")
    private String code;

    @NotNull(message = "Discount percent is required")
    @DecimalMin(value = "1", message = "Discount percent must be at least 1")
    @DecimalMax(value = "100", message = "Discount percent must not exceed 100")
    private Double discountPercent;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @PositiveOrZero(message = "Price order active cannot be negative")
    private Double priceOrderActive;
}
