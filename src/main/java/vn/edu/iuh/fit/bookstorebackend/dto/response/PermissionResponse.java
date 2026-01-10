package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.HttpMethod;

@Data
@JsonPropertyOrder({ "id", "roleCode", "httpMethod", "pathPattern", "active" })
public class PermissionResponse {
    private Long id;
    private String roleCode;
    private HttpMethod httpMethod;
    private String pathPattern;
    private boolean active;
}


