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
    private LocalDate productionDate;
    private String manufacturer;
    private LocalDateTime createdAt;
    private Long bookId;
    private String bookTitle;
    private Long createdById;
    private String createdByName;
    private Long importStockDetailId;
}
