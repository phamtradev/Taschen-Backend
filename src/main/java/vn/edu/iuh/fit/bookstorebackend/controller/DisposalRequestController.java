package vn.edu.iuh.fit.bookstorebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateDisposalRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ProcessDisposalRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.DisposalRequestResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.DisposalRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/disposal-requests")
@RequiredArgsConstructor
public class DisposalRequestController {

    private final DisposalRequestService disposalRequestService;

    @PostMapping
    public ResponseEntity<DisposalRequestResponse> createDisposalRequest(
            @RequestBody CreateDisposalRequestRequest request) throws IdInvalidException {
        // Role: WAREHOUSE_STAFF - Tạo yêu cầu xuất hủy
        DisposalRequestResponse response = disposalRequestService.createDisposalRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-requests")
    public ResponseEntity<List<DisposalRequestResponse>> getMyDisposalRequests() {
        // Xem yêu cầu của chính mình
        return ResponseEntity.ok(disposalRequestService.getMyDisposalRequests());
    }

    @GetMapping
    public ResponseEntity<List<DisposalRequestResponse>> getAllDisposalRequests() {
        // Role: ADMIN - Xem toàn bộ yêu cầu
        return ResponseEntity.ok(disposalRequestService.getAllDisposalRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DisposalRequestResponse> getDisposalRequestById(@PathVariable Long id) throws IdInvalidException {
        return ResponseEntity.ok(disposalRequestService.getDisposalRequestById(id));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<DisposalRequestResponse> approveDisposalRequest(
            @PathVariable Long id,
            @RequestBody ProcessDisposalRequestRequest request) throws IdInvalidException {
        // Role: ADMIN - Duyệt yêu cầu xuất hủy
        return ResponseEntity.ok(disposalRequestService.approveDisposalRequest(id, request));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<DisposalRequestResponse> rejectDisposalRequest(
            @PathVariable Long id,
            @RequestBody ProcessDisposalRequestRequest request) throws IdInvalidException {
        // Role: ADMIN - Từ chối yêu cầu xuất hủy
        return ResponseEntity.ok(disposalRequestService.rejectDisposalRequest(id, request));
    }
}
