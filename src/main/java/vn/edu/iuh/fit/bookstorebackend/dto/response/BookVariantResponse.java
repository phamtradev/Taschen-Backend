package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "bookId", "variantId", "variantFormatCode", "variantFormatName", "price", "stockQuantity" })
public class BookVariantResponse {
    private Long id;
    private Long bookId;
    private Long variantId;
    private String variantFormatCode;
    private String variantFormatName;
    private Double price;
    private Integer stockQuantity;
}
