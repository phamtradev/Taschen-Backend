package vn.edu.iuh.fit.bookstorebackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponse {
    private Long id;
    private String batchCode;
    private int quantity;
    private int remainingQuantity;
    private double importPrice;
    private double sellingPrice;
    private LocalDate productionDate;
    private String manufacturer;
    private LocalDateTime createdAt;
    private Long bookId;
    private String bookTitle;
    private Long createdById;
    private String createdByName;
    private Long importStockDetailId;

    @Builder.Default
    private VariantInfo variant = new VariantInfo();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VariantInfo {
        private Long id;
        private String formatName;
        private String formatCode;
    }
}
