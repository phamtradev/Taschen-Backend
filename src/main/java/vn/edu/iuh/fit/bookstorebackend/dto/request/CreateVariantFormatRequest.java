package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateVariantFormatRequest {
    private String code;
    private String name;
}
