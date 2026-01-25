package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.dto.response.VariantResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;

@Mapper(componentModel = "spring")
public interface VariantMapper {

    @Mapping(target = "bookId", source = "book", qualifiedByName = "bookToId")
    @Mapping(target = "bookTitle", source = "book", qualifiedByName = "bookToTitle")
    VariantResponse toVariantResponse(Variant variant);

    @Named("bookToId")
    default Long bookToId(Book book) {
        return book != null ? book.getId() : null;
    }

    @Named("bookToTitle")
    default String bookToTitle(Book book) {
        return book != null ? book.getTitle() : null;
    }
}
