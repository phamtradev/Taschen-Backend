package vn.edu.iuh.fit.bookstorebackend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BatchDetailResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BatchResponse;
import vn.edu.iuh.fit.bookstorebackend.model.Batch;
import vn.edu.iuh.fit.bookstorebackend.model.BatchDetail;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BatchMapper {

    @Mapping(target = "bookId", expression = "java(batch.getBook().getId())")
    @Mapping(target = "bookTitle", expression = "java(batch.getBook().getTitle())")
    @Mapping(target = "createdById", expression = "java(batch.getCreatedBy().getId())")
    @Mapping(target = "createdByName", expression = "java(batch.getCreatedBy().getFirstName() + \" \" + batch.getCreatedBy().getLastName())")
    @Mapping(target = "importStockDetailId", expression = "java(batch.getImportStockDetail() != null ? batch.getImportStockDetail().getId() : null)")
    @Mapping(target = "variant.id", expression = "java(batch.getVariant().getId())")
    @Mapping(target = "variant.formatName", expression = "java(batch.getVariant().getFormatName())")
    @Mapping(target = "variant.formatCode", expression = "java(batch.getVariant().getFormatCode())")
    BatchResponse toBatchResponse(Batch batch);

    List<BatchResponse> toBatchResponseList(List<Batch> batches);

    @Mapping(target = "batchId", expression = "java(batchDetail.getBatch().getId())")
    @Mapping(target = "batchCode", expression = "java(batchDetail.getBatch().getBatchCode())")
    @Mapping(target = "orderDetailId", expression = "java(batchDetail.getOrderDetail().getId())")
    @Mapping(target = "orderId", expression = "java(batchDetail.getOrderDetail().getOrder().getId())")
    @Mapping(target = "bookId", expression = "java(batchDetail.getOrderDetail().getBook().getId())")
    @Mapping(target = "bookTitle", expression = "java(batchDetail.getOrderDetail().getBook().getTitle())")
    BatchDetailResponse toBatchDetailResponse(BatchDetail batchDetail);

    List<BatchDetailResponse> toBatchDetailResponseList(List<BatchDetail> batchDetails);
}
