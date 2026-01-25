package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.request.AddressRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AddressResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Address;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDefault", ignore = true)
    Address toAddress(AddressRequest request);

    AddressResponse toAddressResponse(Address address);
}
