package vn.edu.iuh.fit.bookstorebackend.dto.request;

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
    private Double price;
    private Integer stockQuantity;
    private String imageUrl;
    private Boolean isActive;
    private List<Long> categoryIds;
    private List<String> variantFormats;
}
