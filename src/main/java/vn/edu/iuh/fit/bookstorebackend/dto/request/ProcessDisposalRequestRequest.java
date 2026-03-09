package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDisposalRequestRequest {
    @Size(max = 500, message = "Response note must not exceed 500 characters")
    private String responseNote;
}
