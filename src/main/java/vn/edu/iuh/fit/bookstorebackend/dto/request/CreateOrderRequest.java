package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    
    private List<Long> cartItemIds;

    private Long addressId;

    private String promotionCode;

    private String paymentMethod;
}

