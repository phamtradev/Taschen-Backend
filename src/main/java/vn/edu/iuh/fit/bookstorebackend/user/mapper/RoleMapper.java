package vn.edu.iuh.fit.bookstorebackend.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.CreateRoleRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.response.RoleResponse;
import vn.edu.iuh.fit.bookstorebackend.user.model.Role;

@Mapper(componentModel = "spring", uses = PermissionMapper.class)
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    Role toRole(CreateRoleRequest request);

    @Mapping(target = "permissions", ignore = true)
    RoleResponse toRoleResponse(Role role);
}
