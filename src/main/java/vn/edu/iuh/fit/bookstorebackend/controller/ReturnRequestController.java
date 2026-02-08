package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateReturnRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ProcessReturnRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ReturnRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.ReturnRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/return-requests")
public class ReturnRequestController {

    private final ReturnRequestService returnRequestService;

    public ReturnRequestController(ReturnRequestService returnRequestService) {
        this.returnRequestService = returnRequestService;
    }

    @PostMapping
    public ResponseEntity<ReturnRequestResponse> createReturnRequest(
            @RequestBody CreateReturnRequestRequest request) throws IdInvalidException {
        // Role: CUSTOMER (chủ đơn) hoặc SELLER - Tạo yêu cầu trả hàng
        ReturnRequestResponse returnRequestResponse = returnRequestService.createReturnRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(returnRequestResponse);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<ReturnRequestResponse>> getMyReturnRequests() {
        List<ReturnRequestResponse> returnRequests = returnRequestService.getMyReturnRequests();
        return ResponseEntity.status(HttpStatus.OK).body(returnRequests);
    }

    @GetMapping
    public ResponseEntity<List<ReturnRequestResponse>> getAllReturnRequests() {
        // Role: ADMIN hoặc SELLER - Lấy tất cả yêu cầu trả hàng
        List<ReturnRequestResponse> returnRequests = returnRequestService.getAllReturnRequests();
        return ResponseEntity.status(HttpStatus.OK).body(returnRequests);
    }

    @PutMapping("/{returnRequestId}/approve")
    public ResponseEntity<ReturnRequestResponse> approveReturnRequest(
            @PathVariable Long returnRequestId,
            @RequestBody ProcessReturnRequestRequest request) throws IdInvalidException {
        // Role: SELLER - Duyệt yêu cầu trả hàng
        ReturnRequestResponse returnRequestResponse = returnRequestService.approveReturnRequest(
                returnRequestId, request);
        return ResponseEntity.status(HttpStatus.OK).body(returnRequestResponse);
    }

    @PutMapping("/{returnRequestId}/reject")
    public ResponseEntity<ReturnRequestResponse> rejectReturnRequest(
            @PathVariable Long returnRequestId,
            @RequestBody ProcessReturnRequestRequest request) throws IdInvalidException {
        // Role: SELLER - Từ chối yêu cầu trả hàng
        ReturnRequestResponse returnRequestResponse = returnRequestService.rejectReturnRequest(
                returnRequestId, request);
        return ResponseEntity.status(HttpStatus.OK).body(returnRequestResponse);
    }
}
