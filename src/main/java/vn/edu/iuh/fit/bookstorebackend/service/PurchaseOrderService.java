package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePurchaseOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PurchaseOrderResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface PurchaseOrderService {

    PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request) throws IdInvalidException;

    List<PurchaseOrderResponse> getAllPurchaseOrders();

    PurchaseOrderResponse getPurchaseOrderById(Long id) throws IdInvalidException;

    void deletePurchaseOrder(Long id) throws IdInvalidException;

    PurchaseOrderResponse approvePurchaseOrder(Long purchaseOrderId, Long approvedById) throws IdInvalidException;

    PurchaseOrderResponse rejectPurchaseOrder(Long purchaseOrderId, Long approvedById) throws IdInvalidException;

    PurchaseOrderResponse cancelPurchaseOrder(Long purchaseOrderId, Long cancelledById, String cancelReason) throws IdInvalidException;

    PurchaseOrderResponse payPurchaseOrder(Long purchaseOrderId, Long paidById) throws IdInvalidException;
}
