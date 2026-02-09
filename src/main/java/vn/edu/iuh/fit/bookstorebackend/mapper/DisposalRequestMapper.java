package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.response.DisposalRequestItemResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.DisposalRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.model.DisposalRequest;
import vn.edu.iuh.fit.bookstorebackend.model.DisposalRequestItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DisposalRequestMapper {

    @Mapping(target = "createdById", expression = "java(request.getCreatedBy().getId())")
    @Mapping(target = "createdByName", expression = "java(request.getCreatedBy().getFirstName() + \" \" + request.getCreatedBy().getLastName())")
    @Mapping(target = "processedById", expression = "java(request.getProcessedBy() != null ? request.getProcessedBy().getId() : null)")
    @Mapping(target = "processedByName", expression = "java(request.getProcessedBy() != null ? request.getProcessedBy().getFirstName() + \" \" + request.getProcessedBy().getLastName() : null)")
    @Mapping(target = "items", source = "items")
    DisposalRequestResponse toResponse(DisposalRequest request);

    List<DisposalRequestResponse> toResponseList(List<DisposalRequest> requests);

    @Mapping(target = "batchId", expression = "java(item.getBatch().getId())")
    @Mapping(target = "batchCode", expression = "java(item.getBatch().getBatchCode())")
    @Mapping(target = "bookId", expression = "java(item.getBatch().getBook().getId())")
    @Mapping(target = "bookTitle", expression = "java(item.getBatch().getBook().getTitle())")
    DisposalRequestItemResponse toItemResponse(DisposalRequestItem item);

    List<DisposalRequestItemResponse> toItemResponseList(List<DisposalRequestItem> items);
}
