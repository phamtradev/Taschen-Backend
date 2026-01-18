package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreatePromotionRequest {
    private String name;
    private String code;
    private Double discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer quantity;
    private Double priceOrderActive;
}
