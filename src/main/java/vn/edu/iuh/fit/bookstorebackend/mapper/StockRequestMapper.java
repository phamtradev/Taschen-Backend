package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.response.StockRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.model.StockRequest;

@Mapper(componentModel = "spring")
public interface StockRequestMapper {

    @Mapping(target = "bookId", expression = "java(stockRequest.getBook().getId())")
    @Mapping(target = "bookTitle", expression = "java(stockRequest.getBook().getTitle())")
    @Mapping(target = "createdById", expression = "java(stockRequest.getCreatedBy().getId())")
    @Mapping(target = "createdByName", expression = "java(stockRequest.getCreatedBy().getFirstName() + \" \" + stockRequest.getCreatedBy().getLastName())")
    @Mapping(target = "processedById", expression = "java(stockRequest.getProcessedBy() != null ? stockRequest.getProcessedBy().getId() : null)")
    @Mapping(target = "processedByName", expression = "java(stockRequest.getProcessedBy() != null ? stockRequest.getProcessedBy().getFirstName() + \" \" + stockRequest.getProcessedBy().getLastName() : null)")
    StockRequestResponse toStockRequestResponse(StockRequest stockRequest);
}
