package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateRoleRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.RoleResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    Role toRole(CreateRoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
