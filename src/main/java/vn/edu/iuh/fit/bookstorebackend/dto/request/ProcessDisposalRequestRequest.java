package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDisposalRequestRequest {
    private String responseNote;
}
