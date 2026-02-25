package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({ "id", "title", "author", "description", "publicationYear", "weightGrams", "pageCount", "price", "stockQuantity", "imageUrl", "isActive", "variantFormats", "categoryIds", "supplierId" })
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
    private List<VariantFormatInfo> variantFormats;
    private List<Long> categoryIds;
    private Long supplierId;
    
    @Data
    public static class VariantFormatInfo {
        private String formatCode;
        private String formatName;
        private Double price;
        private Integer stockQuantity;
    }
}
