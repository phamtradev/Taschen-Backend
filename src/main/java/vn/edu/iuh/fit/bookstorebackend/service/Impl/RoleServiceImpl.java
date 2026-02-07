package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateRoleRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.RoleResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Role;
import vn.edu.iuh.fit.bookstorebackend.mapper.RoleMapper;
import vn.edu.iuh.fit.bookstorebackend.repository.RoleRepository;
import vn.edu.iuh.fit.bookstorebackend.service.RoleService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @Override
    public RoleResponse createRole(CreateRoleRequest request) {
        Role role = roleMapper.toRole(request);
        Role savedRole = roleRepository.save(role);
        return roleMapper.toRoleResponse(savedRole);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(roleMapper::toRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        Role role = findRoleById(id);
        return roleMapper.toRoleResponse(role);
    }

    @Override
    public RoleResponse updateRole(Long id, CreateRoleRequest request) {
        Role role = findRoleById(id);
        updateRoleFields(role, request);
        Role updatedRole = roleRepository.save(role);
        return roleMapper.toRoleResponse(updatedRole);
    }

    @Override
    public void deleteRole(Long id) {
        validateRoleExists(id);
        roleRepository.deleteById(id);
    }
    
    private Role findRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found: " + id));
    }
    
    private void updateRoleFields(Role role, CreateRoleRequest request) {
        if (request.getCode() != null) {
            role.setCode(request.getCode());
        }
        if (request.getName() != null) {
            role.setName(request.getName());
        }
    }
    
    private void validateRoleExists(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new RuntimeException("Role not found: " + id);
        }
    }

    private List<RoleResponse> mapToRoleResponseList(List<Role> roles) {
        return roles.stream()
                .map(roleMapper::toRoleResponse)
                .collect(Collectors.toList());
    }
}


