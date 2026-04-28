package vn.edu.iuh.fit.bookstorebackend.book.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {
    private String code;
    private String name;
}
