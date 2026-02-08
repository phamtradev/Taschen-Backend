package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ReturnToWarehouseRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.model.ReturnToWarehouseRequest;

@Mapper(componentModel = "spring")
public interface ReturnToWarehouseRequestMapper {

    @Mapping(target = "bookId", expression = "java(returnToWarehouseRequest.getBook().getId())")
    @Mapping(target = "bookTitle", expression = "java(returnToWarehouseRequest.getBook().getTitle())")
    @Mapping(target = "createdById", expression = "java(returnToWarehouseRequest.getCreatedBy().getId())")
    @Mapping(target = "createdByName", expression = "java(returnToWarehouseRequest.getCreatedBy().getFirstName() + \" \" + returnToWarehouseRequest.getCreatedBy().getLastName())")
    @Mapping(target = "processedById", expression = "java(returnToWarehouseRequest.getProcessedBy() != null ? returnToWarehouseRequest.getProcessedBy().getId() : null)")
    @Mapping(target = "processedByName", expression = "java(returnToWarehouseRequest.getProcessedBy() != null ? returnToWarehouseRequest.getProcessedBy().getFirstName() + \" \" + returnToWarehouseRequest.getProcessedBy().getLastName() : null)")
    ReturnToWarehouseRequestResponse toReturnToWarehouseRequestResponse(ReturnToWarehouseRequest returnToWarehouseRequest);
}
