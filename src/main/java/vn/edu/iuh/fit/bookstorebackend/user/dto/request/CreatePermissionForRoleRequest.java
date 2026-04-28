package vn.edu.iuh.fit.bookstorebackend.user.dto.request;

import lombok.Getter;
import lombok.Setter;
import vn.edu.iuh.fit.bookstorebackend.shared.common.HttpMethod;

@Getter
@Setter
public class CreatePermissionForRoleRequest {
    private String code;  // VD: "USER_GET_ALL", "BOOK_CREATE"
    private String roleCode;
    private HttpMethod httpMethod;
    private String pathPattern;
    private Boolean active;
}




