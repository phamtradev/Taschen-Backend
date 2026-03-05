package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BatchResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.DisposalRequestItemResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.DisposalRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Batch;
import vn.edu.iuh.fit.bookstorebackend.model.DisposalRequest;
import vn.edu.iuh.fit.bookstorebackend.model.DisposalRequestItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DisposalRequestMapper {
    //map DisposalRequest to DisposalRequestResponse
    @Mapping(target = "createdById", expression = "java(request.getCreatedBy().getId())")
    @Mapping(target = "createdByName", expression = "java(request.getCreatedBy().getFirstName() + \" \" + request.getCreatedBy().getLastName())")
    @Mapping(target = "processedById", expression = "java(request.getProcessedBy() != null ? request.getProcessedBy().getId() : null)")
    @Mapping(target = "processedByName", expression = "java(request.getProcessedBy() != null ? request.getProcessedBy().getFirstName() + \" \" + request.getProcessedBy().getLastName() : null)")
    @Mapping(target = "items", source = "items")
    DisposalRequestResponse toResponse(DisposalRequest request);

    List<DisposalRequestResponse> toResponseList(List<DisposalRequest> requests);

    @Mapping(target = "batchId", expression = "java(item.getBatch().getId())")
    @Mapping(target = "batch", source = "batch", qualifiedByName = "mapBatchToResponse")
    DisposalRequestItemResponse toItemResponse(DisposalRequestItem item);

    List<DisposalRequestItemResponse> toItemResponseList(List<DisposalRequestItem> items);

    @Named("mapBatchToResponse")
    default BatchResponse mapBatchToResponse(Batch batch) {
        if (batch == null) {
            return null;
        }
        return BatchResponse.builder()
                .id(batch.getId())
                .batchCode(batch.getBatchCode())
                .quantity(batch.getQuantity())
                .remainingQuantity(batch.getRemainingQuantity())
                .importPrice(batch.getImportPrice())
                .productionDate(batch.getProductionDate())
                .createdAt(batch.getCreatedAt())
                .bookId(batch.getBook().getId())
                .bookTitle(batch.getBook().getTitle())
                .createdById(batch.getCreatedBy().getId())
                .createdByName(batch.getCreatedBy().getFirstName() + " " + batch.getCreatedBy().getLastName())
                .supplierId(batch.getSupplier().getId())
                .supplierName(batch.getSupplier().getName())
                .variant(BatchResponse.VariantInfo.builder()
                        .id(batch.getVariant().getId())
                        .formatName(batch.getVariant().getFormatName())
                        .formatCode(batch.getVariant().getFormatCode())
                        .build())
                .build();
    }
}
