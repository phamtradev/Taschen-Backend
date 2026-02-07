package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.PaymentMethod;

import java.util.List;

@Data
public class CreateOrderRequest {

    
    private List<Long> cartItemIds;

    private Long addressId;

    private String promotionCode;

    private PaymentMethod paymentMethod;
}

