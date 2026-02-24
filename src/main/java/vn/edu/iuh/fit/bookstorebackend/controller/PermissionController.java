package vn.edu.iuh.fit.bookstorebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePermissionForRoleRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePermissionRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PermissionResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.PermissionService;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<PermissionResponse> createPermission(
            @RequestBody CreatePermissionRequest request) throws IdInvalidException {
        PermissionResponse resp = permissionService.createPermission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable Long id) throws IdInvalidException {
        return ResponseEntity.ok(permissionService.getPermissionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PermissionResponse> updatePermission(
            @PathVariable Long id,
            @RequestBody CreatePermissionRequest request) throws IdInvalidException {
        return ResponseEntity.ok(permissionService.updatePermission(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) throws IdInvalidException {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/assign")
    public ResponseEntity<PermissionResponse> createAndAssignToRole(
            @RequestBody CreatePermissionForRoleRequest request) throws IdInvalidException {
        PermissionResponse resp = permissionService.createAndAssignToRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/role/{code}")
    public ResponseEntity<List<PermissionResponse>> getPermissionsByRoleCode(
            @PathVariable("code") String code) throws IdInvalidException {
        return ResponseEntity.ok(permissionService.getPermissionsByRoleCode(code));
    }
}
