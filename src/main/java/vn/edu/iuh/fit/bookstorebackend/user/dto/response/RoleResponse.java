package vn.edu.iuh.fit.bookstorebackend.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String code;
    private String name;
    private List<PermissionResponse> permissions;
    
    @JsonProperty("permissionCount")
    public int getPermissionCount() {
        return permissions != null ? permissions.size() : 0;
    }
}


