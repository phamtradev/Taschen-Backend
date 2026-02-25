package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBookVariantRequest {
    private Long bookId;
    private Long variantId;
    private Double price;
    private Integer stockQuantity;
}
