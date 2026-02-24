package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.common.HttpMethod;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePermissionForRoleRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePermissionRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PermissionResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.PermissionMapper;
import vn.edu.iuh.fit.bookstorebackend.model.Permission;
import vn.edu.iuh.fit.bookstorebackend.model.Role;
import vn.edu.iuh.fit.bookstorebackend.repository.PermissionRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.RoleRepository;
import vn.edu.iuh.fit.bookstorebackend.service.PermissionService;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final PermissionMapper permissionMapper;

    @Override
    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) throws IdInvalidException {
        Permission permission = buildPermissionFromRequest(request);
        Permission saved = permissionRepository.save(permission);
        return toResponse(saved);
    }

    private Permission buildPermissionFromRequest(CreatePermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission.setActive(getActiveOrDefault(request.getActive()));
        return permission;
    }

    private boolean getActiveOrDefault(Boolean active) {
        return active != null ? active : true;
    }

    @Override
    @Transactional
    public PermissionResponse createAndAssignToRole(CreatePermissionForRoleRequest request) throws IdInvalidException {
        Role role = getRoleOrThrow(request.getRoleCode());
        Permission permission = buildPermissionFromRoleRequest(request);
        Permission saved = permissionRepository.save(permission);
        assignPermissionToRole(role, saved);
        return toResponseWithRole(saved, role.getCode());
    }

    private Permission buildPermissionFromRoleRequest(CreatePermissionForRoleRequest request) {
        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setHttpMethod(request.getHttpMethod());
        permission.setPathPattern(request.getPathPattern());
        permission.setActive(getActiveOrDefault(request.getActive()));
        return permission;
    }

    private void assignPermissionToRole(Role role, Permission permission) {
        role.getPermissions().add(permission);
        roleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionById(Long id) throws IdInvalidException {
        Permission permission = getPermissionOrThrow(id);
        return toResponse(permission);
    }

    private Permission getPermissionOrThrow(Long id) throws IdInvalidException {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Permission not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissionsByRoleCode(String roleCode) throws IdInvalidException {
        Role role = getRoleOrThrow(roleCode);
        return role.getPermissions().stream()
                .map(p -> toResponseWithRole(p, roleCode))
                .collect(Collectors.toList());
    }

    private Role getRoleOrThrow(String roleCode) throws IdInvalidException {
        return roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IdInvalidException("Role not found: " + roleCode));
    }

    @Override
    @Transactional
    public PermissionResponse updatePermission(Long id, CreatePermissionRequest request) throws IdInvalidException {
        Permission permission = getPermissionOrThrow(id);
        updateFields(permission, request);
        Permission updated = permissionRepository.save(permission);
        return toResponse(updated);
    }

    private void updateFields(Permission permission, CreatePermissionRequest request) {
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
    @Transactional
    public void deletePermission(Long id) throws IdInvalidException {
        validateExists(id);
        removeFromAllRoles(id);
        permissionRepository.deleteById(id);
    }

    private void validateExists(Long id) throws IdInvalidException {
        if (!permissionRepository.existsById(id)) {
            throw new IdInvalidException("Permission not found: " + id);
        }
    }

    private void removeFromAllRoles(Long permissionId) {
        List<Role> roles = roleRepository.findByPermissionsId(permissionId);
        if (isEmpty(roles)) {
            return;
        }
        for (Role role : roles) {
            if (role.getPermissions().removeIf(p -> p.getId().equals(permissionId))) {
                roleRepository.save(role);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Set<Long> roleIds, HttpMethod httpMethod, String path) {
        if (isEmpty(roleIds)) {
            return false;
        }
        Set<Role> roles = roleRepository.findByIdIn(roleIds);
        if (isEmpty(roles)) {
            return false;
        }
        return rolesHasMatchingPermission(roles, httpMethod, path);
    }

    private boolean rolesHasMatchingPermission(Set<Role> roles, HttpMethod httpMethod, String path) {
        for (Role role : roles) {
            if (role.getPermissions() == null) {
                continue;
            }
            for (Permission perm : role.getPermissions()) {
                if (isPermissionMatch(perm, httpMethod, path)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isPermissionMatch(Permission perm, HttpMethod httpMethod, String path) {
        return perm.isActive()
            && perm.getHttpMethod() == httpMethod
            && matchPath(perm.getPathPattern(), path);
    }

    private boolean matchPath(String pattern, String path) {
        if (pattern.endsWith("/**")) {
            return path.startsWith(pattern.substring(0, pattern.length() - 3));
        }
        return pattern.equals(path);
    }

    private PermissionResponse toResponse(Permission permission) {
        PermissionResponse response = permissionMapper.toPermissionResponse(permission);
        response.setRoleCode(getRoleCodesOrNull(permission.getId()));
        return response;
    }

    private PermissionResponse toResponseWithRole(Permission permission, String roleCode) {
        PermissionResponse response = new PermissionResponse();
        response.setId(permission.getId());
        response.setCode(permission.getCode());
        response.setHttpMethod(permission.getHttpMethod());
        response.setPathPattern(permission.getPathPattern());
        response.setActive(permission.isActive());
        response.setRoleCode(roleCode);
        return response;
    }

    private String getRoleCodesOrNull(Long permissionId) {
        List<Role> roles = roleRepository.findByPermissionsId(permissionId);
        if (isEmpty(roles)) {
            return null;
        }
        return roles.stream()
                .map(Role::getCode)
                .collect(Collectors.joining(","));
    }

    private <T> boolean isEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    private <T> boolean isEmpty(Set<T> set) {
        return set == null || set.isEmpty();
    }
}
