package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.dto.request.RegisterRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AddressResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AuthenticationResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.RegisterResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.UserResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Address;
import vn.edu.iuh.fit.bookstorebackend.model.Role;
import vn.edu.iuh.fit.bookstorebackend.model.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    User toUser(RegisterRequest request);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStringList")
    @Mapping(target = "addresses", ignore = true)
    RegisterResponse toRegisterResponse(User user);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStringList")
    AuthenticationResponse.UserInfo toUserInfo(User user);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToStringList")
    @Mapping(target = "addresses", source = "addresses", qualifiedByName = "addressesToResponseList")
    UserResponse toUserResponse(User user);

    @Named("rolesToStringList")
    default List<String> rolesToStringList(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getCode)
                .collect(Collectors.toList());
    }

    @Named("addressesToResponseList")
    default List<AddressResponse> addressesToResponseList(List<Address> addresses) {
        if (addresses == null) {
            return null;
        }
        return addresses.stream()
                .map(address -> {
                    AddressResponse addressResponse = new AddressResponse();
                    addressResponse.setId(address.getId());
                    addressResponse.setAddressType(address.getAddressType());
                    addressResponse.setStreet(address.getStreet());
                    addressResponse.setDistrict(address.getDistrict());
                    addressResponse.setWard(address.getWard());
                    addressResponse.setCity(address.getCity());
                    addressResponse.setRecipientName(address.getRecipientName());
                    addressResponse.setPhoneNumber(address.getPhoneNumber());
                    addressResponse.setIsDefault(address.getIsDefault());
                    return addressResponse;
                })
                .collect(Collectors.toList());
    }
}
