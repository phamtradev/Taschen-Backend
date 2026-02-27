package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonPropertyOrder({ "id", "importDate", "received", "supplierId", "supplierName", "createdById", "createdByName", "purchaseOrderId", "details" })
public class ImportStockResponse {

    private Long id;
    private LocalDateTime importDate;
    private boolean received;
    private Long supplierId;
    private String supplierName;
    private Long createdById;
    private String createdByName;
    private Long purchaseOrderId; // PurchaseOrder mà ImportStock này thuộc về
    private List<ImportStockDetailResponse> details;
}
