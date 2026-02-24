package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.common.HttpMethod;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePermissionForRoleRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePermissionRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PermissionResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.Set;
import java.util.List;

public interface PermissionService {

    PermissionResponse createPermission(CreatePermissionRequest request) throws IdInvalidException;

    List<PermissionResponse> getAllPermissions();

    PermissionResponse getPermissionById(Long id) throws IdInvalidException;

    PermissionResponse updatePermission(Long id, CreatePermissionRequest request) throws IdInvalidException;

    void deletePermission(Long id) throws IdInvalidException;

    boolean hasPermission(Set<Long> roleIds, HttpMethod httpMethod, String path);

    PermissionResponse createAndAssignToRole(CreatePermissionForRoleRequest request) throws IdInvalidException;

    List<PermissionResponse> getPermissionsByRoleCode(String roleCode) throws IdInvalidException;
}
