package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.PaymentMethod;

@Data
public class UpdatePaymentMethodRequest {
    private PaymentMethod paymentMethod;
}
