package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateVariantRequest {
    private String formatCode;
    private String formatName;
    private Long bookId; // Optional - can be null
}
