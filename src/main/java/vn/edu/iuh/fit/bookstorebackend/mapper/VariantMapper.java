package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.dto.response.VariantResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;
import vn.edu.iuh.fit.bookstorebackend.model.VariantFormat;

@Mapper(componentModel = "spring")
public interface VariantMapper {

    @Mapping(target = "bookId", source = "book", qualifiedByName = "bookToId")
    @Mapping(target = "bookTitle", source = "book", qualifiedByName = "bookToTitle")
    @Mapping(target = "variantFormat", source = "variantFormat", qualifiedByName = "variantFormatToDTO")
    VariantResponse toVariantResponse(Variant variant);

    @Named("bookToId")
    default Long bookToId(Book book) {
        return book != null ? book.getId() : null;
    }

    @Named("bookToTitle")
    default String bookToTitle(Book book) {
        return book != null ? book.getTitle() : null;
    }

    @Named("variantFormatToDTO")
    default VariantResponse.VariantFormatDTO variantFormatToDTO(VariantFormat variantFormat) {
        if (variantFormat == null) {
            return null;
        }
        VariantResponse.VariantFormatDTO dto = new VariantResponse.VariantFormatDTO();
        dto.setId(variantFormat.getId());
        dto.setCode(variantFormat.getCode());
        dto.setName(variantFormat.getName());
        return dto;
    }
}
