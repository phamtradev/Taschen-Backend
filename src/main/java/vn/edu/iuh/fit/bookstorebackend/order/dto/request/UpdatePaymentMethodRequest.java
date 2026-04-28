package vn.edu.iuh.fit.bookstorebackend.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.shared.common.PaymentMethod;

@Data
public class UpdatePaymentMethodRequest {
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
