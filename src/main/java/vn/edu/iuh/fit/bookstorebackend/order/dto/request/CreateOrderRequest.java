package vn.edu.iuh.fit.bookstorebackend.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.shared.common.PaymentMethod;

import java.util.List;

@Data
public class CreateOrderRequest {

    private List<Long> cartItemIds;

    @Positive(message = "Address ID must be positive")
    private Long addressId;

    @Size(max = 50, message = "Promotion code must not exceed 50 characters")
    private String promotionCode;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
