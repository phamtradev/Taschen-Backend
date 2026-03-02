package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderFromStockRequestRequest {
    private Long stockRequestId;
    private Long supplierId;
    private Long createdById;
    private double importPrice;
    private String note;
}
