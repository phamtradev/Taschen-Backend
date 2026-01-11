package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePermissionRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {

    PermissionResponse createPermission(CreatePermissionRequest request);

    List<PermissionResponse> getAllPermissions();
    
    PermissionResponse getPermissionById(Long id);

    PermissionResponse updatePermission(Long id, CreatePermissionRequest request);

    void deletePermission(Long id);
    
}





