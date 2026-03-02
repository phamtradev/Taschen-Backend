package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ReturnRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.model.OrderDetail;
import vn.edu.iuh.fit.bookstorebackend.model.ReturnRequest;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReturnRequestMapper {

    @Mapping(target = "orderId", expression = "java(returnRequest.getOrder().getId())")
    @Mapping(target = "orderTotal", expression = "java(returnRequest.getOrder().getTotalAmount())")
    @Mapping(target = "createdById", expression = "java(returnRequest.getCreatedBy().getId())")
    @Mapping(target = "createdByName", expression = "java(returnRequest.getCreatedBy().getFirstName() + \" \" + returnRequest.getCreatedBy().getLastName())")
    @Mapping(target = "processedById", expression = "java(returnRequest.getProcessedBy() != null ? returnRequest.getProcessedBy().getId() : null)")
    @Mapping(target = "processedByName", expression = "java(returnRequest.getProcessedBy() != null ? returnRequest.getProcessedBy().getFirstName() + \" \" + returnRequest.getProcessedBy().getLastName() : null)")
    @Mapping(target = "items", expression = "java(mapOrderItems(returnRequest.getOrder()))")
    ReturnRequestResponse toReturnRequestResponse(ReturnRequest returnRequest);

    default List<ReturnRequestResponse.OrderItemInfo> mapOrderItems(vn.edu.iuh.fit.bookstorebackend.model.Order order) {
        if (order == null || order.getOrderDetails() == null) {
            return java.util.Collections.emptyList();
        }
        return order.getOrderDetails().stream()
                .map(this::mapToOrderItemInfo)
                .toList();
    }

    default ReturnRequestResponse.OrderItemInfo mapToOrderItemInfo(OrderDetail orderDetail) {
        if (orderDetail == null) {
            return null;
        }
        ReturnRequestResponse.OrderItemInfo info = new ReturnRequestResponse.OrderItemInfo();
        info.setBookId(orderDetail.getBook().getId());
        info.setBookTitle(orderDetail.getBook().getTitle());
        info.setBookAuthor(orderDetail.getBook().getAuthor());
        info.setQuantity(orderDetail.getQuantity());
        info.setUnitPrice(orderDetail.getPriceAtPurchase());
        info.setTotalPrice(orderDetail.getPriceAtPurchase() * orderDetail.getQuantity());
        return info;
    }
}
