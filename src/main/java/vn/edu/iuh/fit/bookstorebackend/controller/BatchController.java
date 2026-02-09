package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBatchDetailRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBatchRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BatchDetailResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BatchResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.BatchService;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
public class BatchController {

    private final BatchService batchService;

    public BatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @PostMapping
    public ResponseEntity<BatchResponse> createBatch(
            @RequestBody CreateBatchRequest request) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Tạo Batch khi nhập hàng
        BatchResponse batchResponse = batchService.createBatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(batchResponse);
    }

    @GetMapping("/{batchId}")
    public ResponseEntity<BatchResponse> getBatchById(
            @PathVariable Long batchId) throws IdInvalidException {
        BatchResponse batchResponse = batchService.getBatchById(batchId);
        return ResponseEntity.status(HttpStatus.OK).body(batchResponse);
    }

    @GetMapping
    public ResponseEntity<List<BatchResponse>> getAllBatches() {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Lấy tất cả Batch
        List<BatchResponse> batches = batchService.getAllBatches();
        return ResponseEntity.status(HttpStatus.OK).body(batches);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<BatchResponse>> getBatchesByBookId(
            @PathVariable Long bookId) throws IdInvalidException {
        List<BatchResponse> batches = batchService.getBatchesByBookId(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(batches);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BatchResponse>> getBatchesByUserId(
            @PathVariable Long userId) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Lấy Batch theo User
        List<BatchResponse> batches = batchService.getBatchesByUserId(userId);
        return ResponseEntity.status(HttpStatus.OK).body(batches);
    }

    @GetMapping("/book/{bookId}/available")
    public ResponseEntity<List<BatchResponse>> getAvailableBatchesByBookId(
            @PathVariable Long bookId) throws IdInvalidException {
        List<BatchResponse> batches = batchService.getAvailableBatchesByBookId(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(batches);
    }

    @PostMapping("/details")
    public ResponseEntity<BatchDetailResponse> createBatchDetail(
            @RequestBody CreateBatchDetailRequest request) throws IdInvalidException {
        // Hệ thống tự động - Tạo BatchDetail khi bán hàng
        BatchDetailResponse batchDetailResponse = batchService.createBatchDetail(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(batchDetailResponse);
    }

    @GetMapping("/details/order/{orderId}")
    public ResponseEntity<List<BatchDetailResponse>> getBatchDetailsByOrderId(
            @PathVariable Long orderId) throws IdInvalidException {
        List<BatchDetailResponse> batchDetails = batchService.getBatchDetailsByOrderId(orderId);
        return ResponseEntity.status(HttpStatus.OK).body(batchDetails);
    }

    @GetMapping("/{batchId}/details")
    public ResponseEntity<List<BatchDetailResponse>> getBatchDetailsByBatchId(
            @PathVariable Long batchId) throws IdInvalidException {
        List<BatchDetailResponse> batchDetails = batchService.getBatchDetailsByBatchId(batchId);
        return ResponseEntity.status(HttpStatus.OK).body(batchDetails);
    }

    @GetMapping("/details/order-detail/{orderDetailId}")
    public ResponseEntity<List<BatchDetailResponse>> getBatchDetailsByOrderDetailId(
            @PathVariable Long orderDetailId) throws IdInvalidException {
        List<BatchDetailResponse> batchDetails = batchService.getBatchDetailsByOrderDetailId(orderDetailId);
        return ResponseEntity.status(HttpStatus.OK).body(batchDetails);
    }

    @GetMapping("/book/{bookId}/total-stock")
    public ResponseEntity<Integer> calculateTotalStockQuantityByBookId(
            @PathVariable Long bookId) throws IdInvalidException {
        int totalStock = batchService.calculateTotalStockQuantityByBookId(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(totalStock);
    }

    // Role: ADMIN hoặc WAREHOUSE_STAFF
    // Đồng bộ Book.stockQuantity với tổng Batch.remainingQuantity
    // 
    // Vấn đề: Book.stockQuantity và Batch.remainingQuantity có thể không khớp
    // Giải pháp: Tính tổng remainingQuantity từ tất cả Batch và cập nhật Book.stockQuantity
    // Đảm bảo User thấy đúng tồn kho và hệ thống kiểm tra tồn kho chính xác
    @PutMapping("/book/{bookId}/sync-stock")
    public ResponseEntity<Void> syncBookStockQuantity(
            @PathVariable Long bookId) throws IdInvalidException {
        batchService.syncBookStockQuantity(bookId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
