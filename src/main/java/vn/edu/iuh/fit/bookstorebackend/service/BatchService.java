package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBatchDetailRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBatchRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BatchDetailResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BatchResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.Batch;

import java.util.List;

public interface BatchService {

    BatchResponse createBatch(CreateBatchRequest request) throws IdInvalidException;

    BatchResponse getBatchById(Long batchId) throws IdInvalidException;

    List<BatchResponse> getAllBatches();

    List<BatchResponse> getBatchesByBookId(Long bookId) throws IdInvalidException;

    List<BatchResponse> getBatchesByUserId(Long userId) throws IdInvalidException;

    List<BatchResponse> getAvailableBatchesByBookId(Long bookId) throws IdInvalidException;

    BatchDetailResponse createBatchDetail(CreateBatchDetailRequest request) throws IdInvalidException;

    List<BatchDetailResponse> getBatchDetailsByOrderId(Long orderId) throws IdInvalidException;

    List<BatchDetailResponse> getBatchDetailsByBatchId(Long batchId) throws IdInvalidException;

    List<BatchDetailResponse> getBatchDetailsByOrderDetailId(Long orderDetailId) throws IdInvalidException;

    void updateBatchRemainingQuantity(Long batchId, int quantity) throws IdInvalidException;

    Batch getBatchForSale(Long bookId, int quantity, String strategy) throws IdInvalidException;

    boolean isBatchAvailable(Long batchId) throws IdInvalidException;

    int calculateTotalStockQuantityByBookId(Long bookId) throws IdInvalidException;

    void syncBookStockQuantity(Long bookId) throws IdInvalidException;
}
