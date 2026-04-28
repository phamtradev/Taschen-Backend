package vn.edu.iuh.fit.bookstorebackend.book.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBookVariantRequest {
    private Double price;
    private Integer stockQuantity;
}
