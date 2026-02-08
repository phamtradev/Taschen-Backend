package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateReturnToWarehouseRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ProcessReturnToWarehouseRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ReturnToWarehouseRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.ReturnToWarehouseRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/return-to-warehouse-requests")
public class ReturnToWarehouseRequestController {

    private final ReturnToWarehouseRequestService returnToWarehouseRequestService;

    public ReturnToWarehouseRequestController(ReturnToWarehouseRequestService returnToWarehouseRequestService) {
        this.returnToWarehouseRequestService = returnToWarehouseRequestService;
    }

    @PostMapping
    public ResponseEntity<ReturnToWarehouseRequestResponse> createReturnToWarehouseRequest(
            @RequestBody CreateReturnToWarehouseRequestRequest request) throws IdInvalidException {
        // Role: SELLER - Tạo yêu cầu trả hàng về kho
        ReturnToWarehouseRequestResponse returnToWarehouseRequestResponse = returnToWarehouseRequestService.createReturnToWarehouseRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnToWarehouseRequestResponse);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<ReturnToWarehouseRequestResponse>> getMyReturnToWarehouseRequests() {
        // Lấy danh sách yêu cầu trả hàng về kho của user hiện tại
        List<ReturnToWarehouseRequestResponse> returnToWarehouseRequests = returnToWarehouseRequestService.getMyReturnToWarehouseRequests();
        return ResponseEntity.status(HttpStatus.OK).body(returnToWarehouseRequests);
    }

    @GetMapping
    public ResponseEntity<List<ReturnToWarehouseRequestResponse>> getAllReturnToWarehouseRequests() {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Lấy tất cả yêu cầu trả hàng về kho
        List<ReturnToWarehouseRequestResponse> returnToWarehouseRequests = returnToWarehouseRequestService.getAllReturnToWarehouseRequests();
        return ResponseEntity.status(HttpStatus.OK).body(returnToWarehouseRequests);
    }

    @PutMapping("/{returnToWarehouseRequestId}/approve")
    public ResponseEntity<ReturnToWarehouseRequestResponse> approveReturnToWarehouseRequest(
            @PathVariable Long returnToWarehouseRequestId,
            @RequestBody ProcessReturnToWarehouseRequestRequest request) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Duyệt yêu cầu trả hàng về kho và cộng tồn kho
        ReturnToWarehouseRequestResponse returnToWarehouseRequestResponse = returnToWarehouseRequestService.approveReturnToWarehouseRequest(
                returnToWarehouseRequestId, request);
        return ResponseEntity.status(HttpStatus.OK).body(returnToWarehouseRequestResponse);
    }

    @PutMapping("/{returnToWarehouseRequestId}/reject")
    public ResponseEntity<ReturnToWarehouseRequestResponse> rejectReturnToWarehouseRequest(
            @PathVariable Long returnToWarehouseRequestId,
            @RequestBody ProcessReturnToWarehouseRequestRequest request) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Từ chối yêu cầu trả hàng về kho
        ReturnToWarehouseRequestResponse returnToWarehouseRequestResponse = returnToWarehouseRequestService.rejectReturnToWarehouseRequest(
                returnToWarehouseRequestId, request);
        return ResponseEntity.status(HttpStatus.OK).body(returnToWarehouseRequestResponse);
    }
}
