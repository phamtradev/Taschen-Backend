package vn.edu.iuh.fit.bookstorebackend.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.CreatePermissionRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.response.PermissionResponse;
import vn.edu.iuh.fit.bookstorebackend.user.model.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "code", source = "request.code")
    Permission toPermission(CreatePermissionRequest request);

    @Mapping(target = "roleCode", ignore = true)
    PermissionResponse toPermissionResponse(Permission permission);
}
