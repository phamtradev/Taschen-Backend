package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessReturnToWarehouseRequestRequest {
    @Size(max = 500, message = "Response note must not exceed 500 characters")
    private String responseNote;
}
