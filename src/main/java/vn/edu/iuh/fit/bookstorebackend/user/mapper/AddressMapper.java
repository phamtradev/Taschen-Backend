package vn.edu.iuh.fit.bookstorebackend.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.AddressRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.response.AddressResponse;
import vn.edu.iuh.fit.bookstorebackend.user.model.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    Address toAddress(AddressRequest request);

    AddressResponse toAddressResponse(Address address);
}
