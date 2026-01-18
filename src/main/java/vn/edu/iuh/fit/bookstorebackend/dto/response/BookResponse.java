package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@JsonPropertyOrder({ "id", "title", "author", "description", "publicationYear", "weightGrams", "pageCount", "price", "stockQuantity", "imageUrl", "isActive", "variantFormats", "categoryIds" })
public class BookResponse {
    private Long id;
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
    private List<String> variantFormats;
    private Set<Long> categoryIds;
}
