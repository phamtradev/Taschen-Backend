package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;
import vn.edu.iuh.fit.bookstorebackend.common.HttpMethod;

@Getter
@Setter
public class CreatePermissionRequest {
    private String code;  // VD: "USER_GET_ALL", "BOOK_CREATE"
    private HttpMethod httpMethod;
    private String pathPattern;
    private Boolean active;
}








