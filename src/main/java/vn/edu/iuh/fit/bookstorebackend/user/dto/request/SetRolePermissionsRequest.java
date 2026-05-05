package vn.edu.iuh.fit.bookstorebackend.user.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SetRolePermissionsRequest {
    private List<Long> permissionIds;
}
