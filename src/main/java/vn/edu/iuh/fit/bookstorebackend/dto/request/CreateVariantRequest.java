package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateVariantRequest {
    private Long variantFormatId;
    private Long bookId;
}
