package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "bookId", "bookTitle", "variantId", "variantName", "quantity", "importPrice" })
public class ImportStockDetailResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long variantId;
    private String variantName;
    private int quantity;
    private double importPrice;
}
