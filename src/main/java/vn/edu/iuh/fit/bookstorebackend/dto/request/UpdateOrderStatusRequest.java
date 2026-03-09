package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.OrderStatus;

@Data
public class UpdateOrderStatusRequest {
    @NotNull(message = "Status is required")
    private OrderStatus status;
}
