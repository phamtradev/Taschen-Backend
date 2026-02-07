package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ApprovePurchaseOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePurchaseOrderRequest;
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
        PurchaseOrderResponse purchaseOrderResponse = purchaseOrderService.createPurchaseOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(purchaseOrderResponse);
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponse>> getAllPurchaseOrders() {
        List<PurchaseOrderResponse> purchaseOrders = purchaseOrderService.getAllPurchaseOrders();
        return ResponseEntity.status(HttpStatus.OK).body(purchaseOrders);
    }

    @PutMapping("/{purchaseOrderId}/approve")
    public ResponseEntity<PurchaseOrderResponse> approvePurchaseOrder(
            @PathVariable Long purchaseOrderId,
            @RequestBody ApprovePurchaseOrderRequest request) throws IdInvalidException {
        PurchaseOrderResponse purchaseOrderResponse = purchaseOrderService.approvePurchaseOrder(
                purchaseOrderId, request.getApprovedById());
        return ResponseEntity.status(HttpStatus.OK).body(purchaseOrderResponse);
    }

    @PutMapping("/{purchaseOrderId}/reject")
    public ResponseEntity<PurchaseOrderResponse> rejectPurchaseOrder(
            @PathVariable Long purchaseOrderId,
            @RequestBody ApprovePurchaseOrderRequest request) throws IdInvalidException {
        PurchaseOrderResponse purchaseOrderResponse = purchaseOrderService.rejectPurchaseOrder(
                purchaseOrderId, request.getApprovedById());
        return ResponseEntity.status(HttpStatus.OK).body(purchaseOrderResponse);
    }
}
