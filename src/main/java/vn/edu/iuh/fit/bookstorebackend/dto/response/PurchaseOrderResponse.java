package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.PurchaseOrderStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonPropertyOrder({ "id", "createdAt", "approvedAt", "note", "cancelReason", "status", "supplierId", "supplierName", "createdById", "createdByName", "approvedById", "approvedByName", "items" })
public class PurchaseOrderResponse {

    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String note;
    private String cancelReason;
    private PurchaseOrderStatus status;
    private Long supplierId;
    private String supplierName;
    private Long createdById;
    private String createdByName;
    private Long approvedById;
    private String approvedByName;
    private List<PurchaseOrderItemResponse> items;
}
