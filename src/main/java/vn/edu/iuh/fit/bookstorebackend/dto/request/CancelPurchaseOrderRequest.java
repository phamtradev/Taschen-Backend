package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelPurchaseOrderRequest {
    private Long cancelledById;
    private String cancelReason; // Bắt buộc: Lý do hủy đơn
}
