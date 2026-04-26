package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateBookRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 100, message = "Author name must not exceed 100 characters")
    private String author;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Positive(message = "Publication year must be positive")
    private Integer publicationYear;

    @Positive(message = "Weight must be positive")
    private Integer weightGrams;

    @Positive(message = "Page count must be positive")
    private Integer pageCount;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price cannot be negative")
    private Double price;

    private Integer stockQuantity;

    private String imageUrl;

    private Boolean isActive;

    private List<@Positive(message = "Category ID must be positive") Long> categoryIds;
    private List<@Positive(message = "Variant ID must be positive") Long> variantIds;

    @Positive(message = "Supplier ID must be positive")
    private Long supplierId;
}
