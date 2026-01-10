package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePermissionRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PermissionResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Permission;
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

    @Override
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        Permission permission = new Permission();
        permission.setHttpMethod(request.getHttpMethod());
        permission.setPathPattern(request.getPathPattern());
        permission.setActive(request.getActive() != null ? request.getActive() : true);
        Permission saved = permissionRepository.save(permission);
        return toResponse(saved);
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public PermissionResponse getPermissionById(Long id) {
        Permission p = permissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Permission not found: " + id));
        return toResponse(p);
    }

    @Override
    public PermissionResponse updatePermission(Long id, CreatePermissionRequest request) {
        Permission p = permissionRepository.findById(id).orElseThrow(() -> new RuntimeException("Permission not found: " + id));
        if (request.getHttpMethod() != null) p.setHttpMethod(request.getHttpMethod());
        if (request.getPathPattern() != null) p.setPathPattern(request.getPathPattern());
        if (request.getActive() != null) p.setActive(request.getActive());
        Permission updated = permissionRepository.save(p);
        return toResponse(updated);
    }

    @Override
    public void deletePermission(Long id) {
        if (!permissionRepository.existsById(id)) throw new RuntimeException("Permission not found: " + id);

        // xoa permission tu tat ca cac role truoc khi xoa
        List<Role> roles = roleRepository.findByPermissionsId(id);
        if (roles != null && !roles.isEmpty()) {
            for (Role role : roles) {
                boolean removed = role.getPermissions().removeIf(p -> p.getId().equals(id));
                if (removed) {
                    roleRepository.save(role);
                }
            }
        }

        permissionRepository.deleteById(id);
    }

    private PermissionResponse toResponse(Permission p) {
        PermissionResponse r = new PermissionResponse();
        r.setId(p.getId());
        r.setHttpMethod(p.getHttpMethod());
        r.setPathPattern(p.getPathPattern());
        r.setActive(p.isActive());

        // tim cac role co permission nay va set roleCode
        List<Role> roles = roleRepository.findByPermissionsId(p.getId());
        if (roles != null && !roles.isEmpty()) {
            // neu permission thuoc nhieu role noi chung voi dau phay
            String roleCodes = roles.stream()
                    .map(Role::getCode)
                    .collect(Collectors.joining(","));
            r.setRoleCode(roleCodes);
        } else {
            // neu permission khong thuoc role nao, set roleCode thanh null
            r.setRoleCode(null);
        }

        return r;
    }
}


