package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PurchaseOrderItemResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PurchaseOrderResponse;
import vn.edu.iuh.fit.bookstorebackend.model.PurchaseOrder;
import vn.edu.iuh.fit.bookstorebackend.model.PurchaseOrderItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PurchaseOrderMapper {

    @Mapping(target = "supplierId", expression = "java(purchaseOrder.getSupplier().getId())")
    @Mapping(target = "supplierName", expression = "java(purchaseOrder.getSupplier().getName())")
    @Mapping(target = "createdById", expression = "java(purchaseOrder.getCreatedBy().getId())")
    @Mapping(target = "createdByName", expression = "java(purchaseOrder.getCreatedBy().getFirstName() + \" \" + purchaseOrder.getCreatedBy().getLastName())")
    @Mapping(target = "approvedById", expression = "java(purchaseOrder.getApprovedBy() != null ? purchaseOrder.getApprovedBy().getId() : null)")
    @Mapping(target = "approvedByName", expression = "java(purchaseOrder.getApprovedBy() != null ? purchaseOrder.getApprovedBy().getFirstName() + \" \" + purchaseOrder.getApprovedBy().getLastName() : null)")
    @Mapping(target = "items", source = "purchaseOrderItems")
    PurchaseOrderResponse toPurchaseOrderResponse(PurchaseOrder purchaseOrder);

    @Mapping(target = "bookId", expression = "java(item.getBook().getId())")
    @Mapping(target = "bookTitle", expression = "java(item.getBook().getTitle())")
    @Mapping(target = "variantId", expression = "java(item.getVariant() != null ? item.getVariant().getId() : null)")
    @Mapping(target = "variantFormat", expression = "java(item.getVariant() != null ? item.getVariant().getFormatName() : null)")
    PurchaseOrderItemResponse toPurchaseOrderItemResponse(PurchaseOrderItem item);

    List<PurchaseOrderItemResponse> toPurchaseOrderItemResponseList(List<PurchaseOrderItem> items);
}
