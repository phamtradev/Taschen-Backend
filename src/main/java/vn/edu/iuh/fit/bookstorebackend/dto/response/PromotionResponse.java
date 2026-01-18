package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.PromotionStatus;

import java.time.LocalDate;

@Data
@JsonPropertyOrder({ "id", "name", "code", "discountPercent", "startDate", "endDate", "quantity", "isActive", "status", "priceOrderActive", "createdById", "createdByName", "approvedById", "approvedByName" })
public class PromotionResponse {
    private Long id;
    private String name;
    private String code;
    private Double discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer quantity;
    private Boolean isActive;
    private PromotionStatus status;
    private Double priceOrderActive;
    private Long createdById;
    private String createdByName;
    private Long approvedById;
    private String approvedByName;
}
