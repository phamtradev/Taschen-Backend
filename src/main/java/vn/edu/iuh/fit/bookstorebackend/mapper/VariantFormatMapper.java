package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateVariantFormatRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.VariantFormatResponse;
import vn.edu.iuh.fit.bookstorebackend.model.VariantFormat;

@Mapper(componentModel = "spring")
public interface VariantFormatMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "variants", ignore = true)
    VariantFormat toVariantFormat(CreateVariantFormatRequest request);

    VariantFormatResponse toVariantFormatResponse(VariantFormat variantFormat);
}
