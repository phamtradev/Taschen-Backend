package vn.edu.iuh.fit.bookstorebackend.user.service;

import vn.edu.iuh.fit.bookstorebackend.user.dto.request.CreateRoleRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.SetRolePermissionsRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    RoleResponse createRole(CreateRoleRequest request);

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(Long id);

    RoleResponse updateRole(Long id, CreateRoleRequest request);

    void deleteRole(Long id);

    RoleResponse assignPermissionToRole(String roleCode, Long permissionId);

    RoleResponse setRolePermissions(String roleCode, SetRolePermissionsRequest request);
}


