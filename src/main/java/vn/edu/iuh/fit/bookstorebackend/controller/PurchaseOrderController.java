package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ApprovePurchaseOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CancelPurchaseOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePurchaseOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.PayPurchaseOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PurchaseOrderResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.PurchaseOrderService;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> createPurchaseOrder(
            @RequestBody CreatePurchaseOrderRequest request) throws IdInvalidException {
        // Role: ADMIN, WAREHOUSE_STAFF, hoặc SELLER - Tạo đơn đặt hàng từ nhà cung cấp
        PurchaseOrderResponse purchaseOrderResponse = purchaseOrderService.createPurchaseOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderResponse);
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> getAllPurchaseOrders() {
        List<PurchaseOrderResponse> purchaseOrders = purchaseOrderService.getAllPurchaseOrders();
        return ResponseEntity.status(HttpStatus.OK).body(purchaseOrders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponse> getPurchaseOrderById(@PathVariable Long id) throws IdInvalidException {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @PutMapping("/{purchaseOrderId}/approve")
    public ResponseEntity<PurchaseOrderResponse> approvePurchaseOrder(
            @PathVariable Long purchaseOrderId,
            @RequestBody ApprovePurchaseOrderRequest request) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Duyệt đơn để có thể nhập hàng
        PurchaseOrderResponse purchaseOrderResponse = purchaseOrderService.approvePurchaseOrder(
                purchaseOrderId, request.getApprovedById());
        return ResponseEntity.status(HttpStatus.OK).body(purchaseOrderResponse);
    }

    @PutMapping("/{purchaseOrderId}/reject")
    public ResponseEntity<PurchaseOrderResponse> rejectPurchaseOrder(
            @PathVariable Long purchaseOrderId,
            @RequestBody ApprovePurchaseOrderRequest request) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Từ chối đơn, không cho nhập hàng
        PurchaseOrderResponse purchaseOrderResponse = purchaseOrderService.rejectPurchaseOrder(
                purchaseOrderId, request.getApprovedById());
        return ResponseEntity.status(HttpStatus.OK).body(purchaseOrderResponse);
    }

    @PutMapping("/{purchaseOrderId}/cancel")
    public ResponseEntity<PurchaseOrderResponse> cancelPurchaseOrder(
            @PathVariable Long purchaseOrderId,
            @RequestBody CancelPurchaseOrderRequest request) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Hủy đơn đã APPROVED do có vấn đề khi nhập hàng
        PurchaseOrderResponse purchaseOrderResponse = purchaseOrderService.cancelPurchaseOrder(
                purchaseOrderId, request.getCancelledById(), request.getCancelReason());
        return ResponseEntity.status(HttpStatus.OK).body(purchaseOrderResponse);
    }

    @PostMapping("/{purchaseOrderId}/pay")
    public ResponseEntity<PurchaseOrderResponse> payPurchaseOrder(
            @PathVariable Long purchaseOrderId,
            @RequestBody PayPurchaseOrderRequest request) throws IdInvalidException {
        // Role: ADMIN - Thanh toán đơn đã APPROVED
        PurchaseOrderResponse purchaseOrderResponse = purchaseOrderService.payPurchaseOrder(
                purchaseOrderId, request.getPaidById());
        return ResponseEntity.status(HttpStatus.OK).body(purchaseOrderResponse);
    }
}
