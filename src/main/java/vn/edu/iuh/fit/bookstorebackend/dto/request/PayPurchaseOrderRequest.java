package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayPurchaseOrderRequest {
    private Long paidById; // ID của ADMIN thanh toán
}
