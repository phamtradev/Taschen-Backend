package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ApproveStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.RejectStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.StockRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.StockRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/stock-requests")
public class StockRequestController {

    private final StockRequestService stockRequestService;

    public StockRequestController(StockRequestService stockRequestService) {
        this.stockRequestService = stockRequestService;
    }

    @PostMapping
    public ResponseEntity<StockRequestResponse> createStockRequest(
            @RequestBody CreateStockRequestRequest request) throws IdInvalidException {
        // Role: SELLER - Tạo yêu cầu nhập hàng
        StockRequestResponse stockRequestResponse = stockRequestService.createStockRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(stockRequestResponse);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<StockRequestResponse>> getMyStockRequest(
            @RequestParam Long userId) {
        // Lấy danh sách yêu cầu nhập hàng của user hiện tại
        List<StockRequestResponse> stockRequests = stockRequestService.getMyStockRequest(userId);
        return ResponseEntity.status(HttpStatus.OK).body(stockRequests);
    }

    @GetMapping
    public ResponseEntity<List<StockRequestResponse>> getAllStockRequest() {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Lấy tất cả yêu cầu nhập hàng
        List<StockRequestResponse> stockRequests = stockRequestService.getAllStockRequest();
        return ResponseEntity.status(HttpStatus.OK).body(stockRequests);
    }

    @PutMapping("/{stockRequestId}/approve")
    public ResponseEntity<StockRequestResponse> approveStockRequest(
            @PathVariable Long stockRequestId,
            @RequestBody ApproveStockRequestRequest request) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Duyệt yêu cầu nhập hàng
        StockRequestResponse stockRequestResponse = stockRequestService.approveStockRequest(
                stockRequestId, request);
        return ResponseEntity.status(HttpStatus.OK).body(stockRequestResponse);
    }

    @PutMapping("/{stockRequestId}/reject")
    public ResponseEntity<StockRequestResponse> rejectStockRequest(
            @PathVariable Long stockRequestId,
            @RequestBody RejectStockRequestRequest request) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Từ chối yêu cầu nhập hàng
        StockRequestResponse stockRequestResponse = stockRequestService.rejectStockRequest(
                stockRequestId, request);
        return ResponseEntity.status(HttpStatus.OK).body(stockRequestResponse);
    }
}
