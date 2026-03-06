package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BatchResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.DisposalRequestItemResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.DisposalRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.DisposalRequestResponse.UserInfo;
import vn.edu.iuh.fit.bookstorebackend.model.Batch;
import vn.edu.iuh.fit.bookstorebackend.model.DisposalRequest;
import vn.edu.iuh.fit.bookstorebackend.model.DisposalRequestItem;
import vn.edu.iuh.fit.bookstorebackend.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DisposalRequestMapper {
    //map DisposalRequest to DisposalRequestResponse
    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "mapUserToUserInfo")
    @Mapping(target = "processedBy", source = "processedBy", qualifiedByName = "mapUserToUserInfo")
    @Mapping(target = "items", source = "items")
    DisposalRequestResponse toResponse(DisposalRequest request);

    List<DisposalRequestResponse> toResponseList(List<DisposalRequest> requests);

    @Mapping(target = "batchId", expression = "java(item.getBatch().getId())")
    @Mapping(target = "batch", source = "batch", qualifiedByName = "mapBatchToResponse")
    DisposalRequestItemResponse toItemResponse(DisposalRequestItem item);

    List<DisposalRequestItemResponse> toItemResponseList(List<DisposalRequestItem> items);

    @Named("mapUserToUserInfo")
    default UserInfo mapUserToUserInfo(User user) {
        if (user == null) {
            return null;
        }
        return UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .build();
    }

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
