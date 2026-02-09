package vn.edu.iuh.fit.bookstorebackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisposalRequestItemResponse {
    private Long id;
    private int quantity;
    private Long batchId;
    private String batchCode;
    private Long bookId;
    private String bookTitle;
}
