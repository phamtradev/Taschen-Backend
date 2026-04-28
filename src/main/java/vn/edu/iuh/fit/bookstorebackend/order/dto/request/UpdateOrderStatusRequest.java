package vn.edu.iuh.fit.bookstorebackend.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.shared.common.OrderStatus;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "Status is required")
    private OrderStatus status;
}
