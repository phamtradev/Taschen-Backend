package vn.edu.iuh.fit.bookstorebackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveStockResponse {

    private Long importStockId;
    private boolean received;
    private List<BatchReceiveResult> batchResults;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchReceiveResult {
        private Long batchId;
        private String batchCode;
        private Long bookId;
        private String bookTitle;
        private Long variantId;
        private String variantName;
        private int quantity;
        private double importPrice;
        private boolean isNew; // true = batch mới, false = batch cũ được cộng dồn
    }
}
