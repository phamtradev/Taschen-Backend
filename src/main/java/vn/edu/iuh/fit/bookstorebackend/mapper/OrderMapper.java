package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.dto.response.OrderDetailResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.OrderResponse;
import vn.edu.iuh.fit.bookstorebackend.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "userId", source = "user", qualifiedByName = "userToId")
    @Mapping(target = "userName", source = "user", qualifiedByName = "userToDisplayName")
    @Mapping(target = "promotionId", source = "promotion", qualifiedByName = "promotionToId")
    @Mapping(target = "promotionCode", source = "promotion", qualifiedByName = "promotionToCode")
    @Mapping(target = "addressId", source = "deliveryAddress", qualifiedByName = "addressToId")
    @Mapping(target = "deliveryAddress", source = "deliveryAddress", qualifiedByName = "addressToStreet")
    @Mapping(target = "orderDetails", source = "orderDetails", qualifiedByName = "orderDetailsToResponseList")
    OrderResponse toOrderResponse(Order order);

    @Named("userToId")
    default Long userToId(User user) {
        return user != null ? user.getId() : null;
    }

    @Named("userToDisplayName")
    default String userToDisplayName(User user) {
        if (user == null) {
            return null;
        }
        if (user.getFirstName() != null || user.getLastName() != null) {
            String firstName = user.getFirstName() != null ? user.getFirstName() : "";
            String lastName = user.getLastName() != null ? user.getLastName() : "";
            return (firstName + " " + lastName).trim();
        }
        return user.getEmail();
    }

    @Named("promotionToId")
    default Long promotionToId(Promotion promotion) {
        return promotion != null ? promotion.getId() : null;
    }

    @Named("promotionToCode")
    default String promotionToCode(Promotion promotion) {
        return promotion != null ? promotion.getCode() : null;
    }

    @Named("addressToId")
    default Long addressToId(Address address) {
        return address != null ? address.getId() : null;
    }

    @Named("addressToStreet")
    default String addressToStreet(Address address) {
        return address != null ? address.getStreet() : null;
    }

    @Named("orderDetailsToResponseList")
    default List<OrderDetailResponse> orderDetailsToResponseList(List<OrderDetail> orderDetails) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return new ArrayList<>();
        }
        return orderDetails.stream()
                .map(this::toOrderDetailResponse)
                .collect(Collectors.toList());
    }

    default OrderDetailResponse toOrderDetailResponse(OrderDetail detail) {
        if (detail == null) {
            return null;
        }
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(detail.getId());
        response.setPriceAtPurchase(detail.getPriceAtPurchase());
        response.setQuantity(detail.getQuantity());
        response.setTotalPrice(detail.getPriceAtPurchase() * detail.getQuantity());

        if (detail.getBook() != null) {
            response.setBookId(detail.getBook().getId());
            response.setBookTitle(detail.getBook().getTitle());
        }

        return response;
    }
}
