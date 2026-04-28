package vn.edu.iuh.fit.bookstorebackend.book.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVariantRequest {
    private String formatCode;
    private String formatName;
}
