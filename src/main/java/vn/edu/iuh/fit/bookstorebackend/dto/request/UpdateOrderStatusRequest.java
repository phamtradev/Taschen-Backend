package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.OrderStatus;

@Data
public class UpdateOrderStatusRequest {
    private OrderStatus status;
}
