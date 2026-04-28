package vn.edu.iuh.fit.bookstorebackend.book.mapper;

import org.mapstruct.Mapper;
import vn.edu.iuh.fit.bookstorebackend.book.dto.response.VariantResponse;
import vn.edu.iuh.fit.bookstorebackend.book.model.Variant;

@Mapper(componentModel = "spring")
public interface VariantMapper {

    VariantResponse toVariantResponse(Variant variant);
}
