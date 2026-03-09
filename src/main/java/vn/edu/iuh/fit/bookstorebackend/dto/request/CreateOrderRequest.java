package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.PaymentMethod;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotEmpty(message = "Cart items are required")
    private List<Long> cartItemIds;

    @NotNull(message = "Address ID is required")
    private Long addressId;

    @Size(max = 50, message = "Promotion code must not exceed 50 characters")
    private String promotionCode;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}

