package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveStockRequestRequest {
    private Long processedById;
    private String responseMessage;
}
