package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ImportStockDetailResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ImportStockResponse;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStock;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStockDetail;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImportStockMapper {

    @Mapping(target = "supplierId", expression = "java(importStock.getSupplier().getId())")
    @Mapping(target = "supplierName", expression = "java(importStock.getSupplier().getName())")
    @Mapping(target = "createdById", expression = "java(importStock.getCreatedBy().getId())")
    @Mapping(target = "createdByName", expression = "java(importStock.getCreatedBy().getFirstName() + \" \" + importStock.getCreatedBy().getLastName())")
    @Mapping(target = "purchaseOrderId", expression = "java(importStock.getPurchaseOrder() != null ? importStock.getPurchaseOrder().getId() : null)")
    @Mapping(target = "received", expression = "java(importStock.isReceived())")
    @Mapping(target = "details", source = "importStockDetails")
    ImportStockResponse toImportStockResponse(ImportStock importStock);

    @Mapping(target = "bookId", expression = "java(detail.getBook().getId())")
    @Mapping(target = "bookTitle", expression = "java(detail.getBook().getTitle())")
    @Mapping(target = "variantId", expression = "java(detail.getVariant().getId())")
    @Mapping(target = "variantName", expression = "java(detail.getVariant().getName())")
    ImportStockDetailResponse toImportStockDetailResponse(ImportStockDetail detail);

    List<ImportStockDetailResponse> toImportStockDetailResponseList(List<ImportStockDetail> details);
}
