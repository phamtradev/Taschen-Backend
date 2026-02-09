package vn.edu.iuh.fit.bookstorebackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDetailResponse {
    private Long id;
    private int quantity;
    private Long batchId;
    private String batchCode;
    private Long orderDetailId;
    private Long orderId;
    private Long bookId;
    private String bookTitle;
}
