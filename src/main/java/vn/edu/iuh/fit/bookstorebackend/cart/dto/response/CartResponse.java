package vn.edu.iuh.fit.bookstorebackend.cart.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({ "id", "userId", "totalPrice", "items" })
public class CartResponse {
    private Long id;
    private Long userId;
    private Double totalPrice;
    private List<CartItemResponse> items;
}
