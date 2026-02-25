package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.dto.response.VariantResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VariantMapper {

    @Mapping(target = "bookIds", source = "books", qualifiedByName = "booksToIds")
    VariantResponse toVariantResponse(Variant variant);

    @Named("booksToIds")
    default List<Long> booksToIds(List<Book> books) {
        if (books == null) {
            return null;
        }
        return books.stream()
                .map(Book::getId)
                .toList();
    }
}
