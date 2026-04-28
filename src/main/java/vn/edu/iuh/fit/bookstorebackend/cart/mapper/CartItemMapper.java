package vn.edu.iuh.fit.bookstorebackend.cart.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.cart.dto.response.CartItemResponse;
import vn.edu.iuh.fit.bookstorebackend.book.model.Book;
import vn.edu.iuh.fit.bookstorebackend.cart.model.CartItem;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "bookId", source = "book", qualifiedByName = "bookToId")
    @Mapping(target = "bookTitle", source = "book", qualifiedByName = "bookToTitle")
    CartItemResponse toCartItemResponse(CartItem cartItem);

    @Named("bookToId")
    default Long bookToId(Book book) {
        return book != null ? book.getId() : null;
    }

    @Named("bookToTitle")
    default String bookToTitle(Book book) {
        return book != null ? book.getTitle() : null;
    }
}
