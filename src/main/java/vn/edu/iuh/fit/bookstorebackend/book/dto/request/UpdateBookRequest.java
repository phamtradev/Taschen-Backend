package vn.edu.iuh.fit.bookstorebackend.book.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateBookRequest {
    private String title;
    private String author;
    private String description;
    private Integer publicationYear;
    private Integer weightGrams;
    private Integer pageCount;
    @PositiveOrZero(message = "Price must be zero or positive")
    private Double price;
    @PositiveOrZero(message = "Stock quantity must be zero or positive")
    private Integer stockQuantity;
    private String imageUrl;
    private Boolean isActive;
    private List<Long> categoryIds;
    private List<Long> variantIds;
    private Long formatId;
    private Long supplierId;
}
