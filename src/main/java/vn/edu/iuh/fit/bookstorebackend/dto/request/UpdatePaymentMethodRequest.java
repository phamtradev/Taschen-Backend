package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.PaymentMethod;

@Data
public class UpdatePaymentMethodRequest {
    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}
