package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "bookId", "bookTitle", "quantity", "unitPrice", "totalPrice" })
public class CartItemResponse {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
}
