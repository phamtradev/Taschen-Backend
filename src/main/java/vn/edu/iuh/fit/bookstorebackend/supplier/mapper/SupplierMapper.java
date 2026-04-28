package vn.edu.iuh.fit.bookstorebackend.supplier.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.supplier.dto.request.CreateSupplierRequest;
import vn.edu.iuh.fit.bookstorebackend.supplier.dto.response.SupplierResponse;
import vn.edu.iuh.fit.bookstorebackend.supplier.model.Supplier;

@Mapper(componentModel = "spring")
public interface SupplierMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Supplier toSupplier(CreateSupplierRequest request);

    SupplierResponse toSupplierResponse(Supplier supplier);
}

