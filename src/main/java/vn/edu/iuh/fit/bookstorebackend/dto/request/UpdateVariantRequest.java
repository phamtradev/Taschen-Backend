package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVariantRequest {
    private String format;
    private Long bookId;
}
