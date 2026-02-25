package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBookVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BookVariantResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.BookVariant;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;

@Mapper(componentModel = "spring")
public interface BookVariantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "book", ignore = true)
    @Mapping(target = "variant", ignore = true)
    BookVariant toBookVariant(CreateBookVariantRequest request);

    @Mapping(target = "bookId", source = "book", qualifiedByName = "bookToId")
    @Mapping(target = "variantId", source = "variant", qualifiedByName = "variantToId")
    @Mapping(target = "variantFormatCode", source = "variant", qualifiedByName = "variantToFormatCode")
    @Mapping(target = "variantFormatName", source = "variant", qualifiedByName = "variantToFormatName")
    BookVariantResponse toBookVariantResponse(BookVariant bookVariant);

    @Named("bookToId")
    default Long bookToId(Book book) {
        return book != null ? book.getId() : null;
    }

    @Named("variantToId")
    default Long variantToId(Variant variant) {
        return variant != null ? variant.getId() : null;
    }

    @Named("variantToFormatCode")
    default String variantToFormatCode(Variant variant) {
        return variant != null ? variant.getFormatCode() : null;
    }

    @Named("variantToFormatName")
    default String variantToFormatName(Variant variant) {
        return variant != null ? variant.getFormatName() : null;
    }
}
