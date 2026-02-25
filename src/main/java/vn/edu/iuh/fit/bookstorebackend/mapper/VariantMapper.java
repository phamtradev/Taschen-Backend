package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import vn.edu.iuh.fit.bookstorebackend.dto.response.VariantResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;

@Mapper(componentModel = "spring")
public interface VariantMapper {

    VariantResponse toVariantResponse(Variant variant);
}
