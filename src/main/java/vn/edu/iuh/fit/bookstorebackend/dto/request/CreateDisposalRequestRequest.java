package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDisposalRequestRequest {
    @NotBlank(message = "Reason is required")
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;

    @NotEmpty(message = "Items are required")
    @Valid
    private List<DisposalItemRequest> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisposalItemRequest {
        @NotNull(message = "Batch ID is required")
        private Long batchId;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private int quantity;
    }
}
