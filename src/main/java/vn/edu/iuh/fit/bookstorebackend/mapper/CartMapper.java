package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CartItemResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CartResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Cart;
import vn.edu.iuh.fit.bookstorebackend.model.CartItem;
import vn.edu.iuh.fit.bookstorebackend.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {

    @Mapping(target = "userId", source = "user", qualifiedByName = "userToId")
    @Mapping(target = "items", source = "items", qualifiedByName = "cartItemsToResponseList")
    CartResponse toCartResponse(Cart cart);

    @Named("userToId")
    default Long userToId(User user) {
        return user != null ? user.getId() : null;
    }

    @Named("cartItemsToResponseList")
    default List<CartItemResponse> cartItemsToResponseList(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        return items.stream()
                .map(item -> {
                    CartItemResponse response = new CartItemResponse();
                    response.setId(item.getId());
                    response.setBookId(item.getBook() != null ? item.getBook().getId() : null);
                    response.setBookTitle(item.getBook() != null ? item.getBook().getTitle() : null);
                    response.setQuantity(item.getQuantity());
                    response.setUnitPrice(item.getUnitPrice());
                    response.setTotalPrice(item.getTotalPrice());
                    return response;
                })
                .collect(Collectors.toList());
    }
}
