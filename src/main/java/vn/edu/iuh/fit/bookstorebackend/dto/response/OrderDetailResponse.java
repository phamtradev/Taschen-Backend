package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "bookId", "bookTitle", "priceAtPurchase", "quantity", "totalPrice" })
public class OrderDetailResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private Double priceAtPurchase;
    private Integer quantity;
    private Double totalPrice;
}

