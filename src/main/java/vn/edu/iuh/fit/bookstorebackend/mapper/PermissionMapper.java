package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePermissionRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PermissionResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Permission toPermission(CreatePermissionRequest request);

    @Mapping(target = "roleCode", ignore = true)
    PermissionResponse toPermissionResponse(Permission permission);
}
