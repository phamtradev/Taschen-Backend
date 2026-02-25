package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateImportStockRequest {

    private Long supplierId;
    private Long createdById;
    private Long purchaseOrderId; // importStock phải link với PurchaseOrder đã APPROVED
    private List<ImportStockDetailRequest> details; // Optional: nếu null/empty, tự động lấy từ PurchaseOrder

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportStockDetailRequest {
        private Long bookId;
        private Long variantId;
        private int quantity;
        private double importPrice;
    }
}
