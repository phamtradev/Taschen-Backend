package vn.edu.iuh.fit.bookstorebackend.supplier.service;

import vn.edu.iuh.fit.bookstorebackend.supplier.dto.request.CreatePurchaseOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.supplier.dto.request.CreatePurchaseOrderFromStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.supplier.dto.response.PurchaseOrderResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface PurchaseOrderService {

    PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request) throws IdInvalidException;

    PurchaseOrderResponse createPurchaseOrderFromStockRequest(CreatePurchaseOrderFromStockRequestRequest request) throws IdInvalidException;

    List<PurchaseOrderResponse> getAllPurchaseOrders();

    PurchaseOrderResponse getPurchaseOrderById(Long id) throws IdInvalidException;

    void deletePurchaseOrder(Long id) throws IdInvalidException;

    PurchaseOrderResponse approvePurchaseOrder(Long purchaseOrderId, Long approvedById) throws IdInvalidException;

    PurchaseOrderResponse rejectPurchaseOrder(Long purchaseOrderId, Long approvedById) throws IdInvalidException;

    PurchaseOrderResponse cancelPurchaseOrder(Long purchaseOrderId, Long cancelledById, String cancelReason) throws IdInvalidException;

    PurchaseOrderResponse payPurchaseOrder(Long purchaseOrderId, Long paidById) throws IdInvalidException;
}
