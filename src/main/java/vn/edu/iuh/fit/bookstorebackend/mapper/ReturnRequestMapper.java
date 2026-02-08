package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ReturnRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.model.ReturnRequest;

@Mapper(componentModel = "spring")
public interface ReturnRequestMapper {

    @Mapping(target = "orderId", expression = "java(returnRequest.getOrder().getId())")
    @Mapping(target = "orderTotal", expression = "java(returnRequest.getOrder().getTotalAmount())")
    @Mapping(target = "createdById", expression = "java(returnRequest.getCreatedBy().getId())")
    @Mapping(target = "createdByName", expression = "java(returnRequest.getCreatedBy().getFirstName() + \" \" + returnRequest.getCreatedBy().getLastName())")
    @Mapping(target = "processedById", expression = "java(returnRequest.getProcessedBy() != null ? returnRequest.getProcessedBy().getId() : null)")
    @Mapping(target = "processedByName", expression = "java(returnRequest.getProcessedBy() != null ? returnRequest.getProcessedBy().getFirstName() + \" \" + returnRequest.getProcessedBy().getLastName() : null)")
    ReturnRequestResponse toReturnRequestResponse(ReturnRequest returnRequest);
}
