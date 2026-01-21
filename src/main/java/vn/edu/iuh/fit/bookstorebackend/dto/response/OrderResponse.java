package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonPropertyOrder({
        "id",
        "userId",
        "userName",
        "orderDate",
        "totalAmount",
        "status",
        "paymentMethod",
        "paymentCode",
        "promotionId",
        "promotionCode",
        "addressId",
        "deliveryAddress",
        "orderDetails"
})
public class OrderResponse {

    private Long id;
    private Long userId;
    private String userName;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private OrderStatus status;
    private String paymentMethod;
    private String paymentCode;

    private Long promotionId;
    private String promotionCode;

    private Long addressId;
    private String deliveryAddress;

    private List<OrderDetailResponse> orderDetails;
}

