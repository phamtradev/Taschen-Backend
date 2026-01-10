package vn.edu.iuh.fit.bookstorebackend.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePermissionRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PermissionResponse;
import vn.edu.iuh.fit.bookstorebackend.service.PermissionService;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePermissionForRoleRequest;
import vn.edu.iuh.fit.bookstorebackend.repository.RoleRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.PermissionRepository;
import vn.edu.iuh.fit.bookstorebackend.model.Role;
import vn.edu.iuh.fit.bookstorebackend.model.Permission;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @PostMapping
    public ResponseEntity<PermissionResponse> createPermission(@RequestBody CreatePermissionRequest request) {
        PermissionResponse resp = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAllPermission() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable Long id) {
        return ResponseEntity.ok(permissionService.getPermissionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PermissionResponse> updatePermission(@PathVariable Long id, @RequestBody CreatePermissionRequest request) {
        return ResponseEntity.ok(permissionService.updatePermission(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    public ResponseEntity<PermissionResponse> createAndAssignToRole(@RequestBody CreatePermissionForRoleRequest request) {
        Role role = roleRepository.findByCode(request.getRoleCode())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleCode()));

        Permission permission = new Permission(); //tao moi permission
        permission.setHttpMethod(request.getHttpMethod());
        permission.setPathPattern(request.getPathPattern());
        permission.setActive(request.getActive() != null ? request.getActive() : true);
        Permission saved = permissionRepository.save(permission);

        role.getPermissions().add(saved);
        roleRepository.save(role);

        PermissionResponse resp = new PermissionResponse(); //chuyen doi tu model sang dto
        resp.setId(saved.getId());
        resp.setRoleCode(role.getCode());
        resp.setHttpMethod(saved.getHttpMethod());
        resp.setPathPattern(saved.getPathPattern());
        resp.setActive(saved.isActive()); //set active cua permission
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }
    
    @GetMapping("role/{code}")
    public ResponseEntity<List<PermissionResponse>> getPermissionsByRoleCode(@PathVariable("code") String code) {
        Role role = roleRepository.findByCode(code).orElse(null);
        if (role == null) {
            return ResponseEntity.notFound().build();
        }
        List<Permission> permissions = role.getPermissions();
        List<PermissionResponse> resp = permissions.stream().map(p -> {
            PermissionResponse permissionResponse = new PermissionResponse();
            permissionResponse.setId(p.getId());
            permissionResponse.setRoleCode(role.getCode());
            permissionResponse.setHttpMethod(p.getHttpMethod());
            permissionResponse.setPathPattern(p.getPathPattern());
            permissionResponse.setActive(p.isActive());
            return permissionResponse;
        }).toList();
        return ResponseEntity.ok(resp);
    }
}


