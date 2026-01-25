package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePermissionRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PermissionResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Permission;
import vn.edu.iuh.fit.bookstorebackend.mapper.PermissionMapper;
import vn.edu.iuh.fit.bookstorebackend.repository.PermissionRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.RoleRepository;
import vn.edu.iuh.fit.bookstorebackend.service.PermissionService;

import java.util.List;
import java.util.stream.Collectors;
import vn.edu.iuh.fit.bookstorebackend.model.Role;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        Permission permission = createPermissionFromRequest(request);
        Permission savedPermission = permissionRepository.save(permission);
        return buildPermissionResponse(savedPermission);
    }
    
    private Permission createPermissionFromRequest(CreatePermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission.setActive(request.getActive() != null ? request.getActive() : true);
        return permission;
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::buildPermissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PermissionResponse getPermissionById(Long id) {
        Permission permission = findPermissionById(id);
        return buildPermissionResponse(permission);
    }
    
    private Permission findPermissionById(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permission not found: " + id));
    }

    @Override
    public PermissionResponse updatePermission(Long id, CreatePermissionRequest request) {
        Permission permission = findPermissionById(id);
        updatePermissionFields(permission, request);
        Permission updatedPermission = permissionRepository.save(permission);
        return buildPermissionResponse(updatedPermission);
    }
    
    private void updatePermissionFields(Permission permission, CreatePermissionRequest request) {
        if (request.getHttpMethod() != null) {
            permission.setHttpMethod(request.getHttpMethod());
        }
        if (request.getPathPattern() != null) {
            permission.setPathPattern(request.getPathPattern());
        }
        if (request.getActive() != null) {
            permission.setActive(request.getActive());
        }
    }

    @Override
    public void deletePermission(Long id) {
        validatePermissionExists(id);
        removePermissionFromRoles(id);
        permissionRepository.deleteById(id);
    }
    
    private void validatePermissionExists(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new RuntimeException("Permission not found: " + id);
        }
    }
    
    private void removePermissionFromRoles(Long permissionId) {
        List<Role> roles = roleRepository.findByPermissionsId(permissionId);
        if (roles == null || roles.isEmpty()) {
            return;
        }
        
        for (Role role : roles) {
            boolean removed = role.getPermissions().removeIf(permission -> permission.getId().equals(permissionId));
            if (removed) {
                roleRepository.save(role);
            }
        }
    }

    private PermissionResponse buildPermissionResponse(Permission permission) {
        PermissionResponse response = permissionMapper.toPermissionResponse(permission);
        String roleCodes = getRoleCodesForPermission(permission.getId());
        response.setRoleCode(roleCodes);
        return response;
    }
    
    private String getRoleCodesForPermission(Long permissionId) {
        List<Role> roles = roleRepository.findByPermissionsId(permissionId);
        if (roles == null || roles.isEmpty()) {
            return null;
        }
        return roles.stream()
                .map(Role::getCode)
                .collect(Collectors.joining(","));
    }
}


