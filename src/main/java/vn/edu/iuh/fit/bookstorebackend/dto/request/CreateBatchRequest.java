package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBatchRequest {

    private Long bookId;
    private Long importStockDetailId; // Optional: Link với ImportStockDetail
    private Long createdById;
    private Integer quantity;
    private Double importPrice;
    private LocalDate productionDate;
    private String manufacturer;
    private String batchCode; // Optional: Nếu không có sẽ tự động generate
}
