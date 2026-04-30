package vn.edu.iuh.fit.bookstorebackend.supplier.service.impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookstorebackend.shared.dto.WsEvent;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.shared.common.PurchaseOrderStatus;
import vn.edu.iuh.fit.bookstorebackend.shared.common.StockRequestStatus;
import vn.edu.iuh.fit.bookstorebackend.supplier.dto.request.CreatePurchaseOrderFromStockRequestRequest;
import vn.edu.iuh.fit.bookstorebackend.supplier.dto.request.CreatePurchaseOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.supplier.dto.response.PurchaseOrderResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.supplier.mapper.PurchaseOrderMapper;
import vn.edu.iuh.fit.bookstorebackend.book.model.Book;
import vn.edu.iuh.fit.bookstorebackend.supplier.model.PurchaseOrder;
import vn.edu.iuh.fit.bookstorebackend.supplier.model.PurchaseOrderItem;
import vn.edu.iuh.fit.bookstorebackend.inventory.model.StockRequest;
import vn.edu.iuh.fit.bookstorebackend.supplier.model.Supplier;
import vn.edu.iuh.fit.bookstorebackend.user.model.User;
import vn.edu.iuh.fit.bookstorebackend.book.model.Variant;
import vn.edu.iuh.fit.bookstorebackend.book.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.book.repository.BookVariantRepository;
import vn.edu.iuh.fit.bookstorebackend.supplier.repository.PurchaseOrderItemRepository;
import vn.edu.iuh.fit.bookstorebackend.supplier.repository.PurchaseOrderRepository;
import vn.edu.iuh.fit.bookstorebackend.inventory.repository.StockRequestRepository;
import vn.edu.iuh.fit.bookstorebackend.supplier.repository.SupplierRepository;
import vn.edu.iuh.fit.bookstorebackend.user.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.book.repository.VariantRepository;
import vn.edu.iuh.fit.bookstorebackend.notification.service.NotificationService;
import vn.edu.iuh.fit.bookstorebackend.supplier.service.PurchaseOrderService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final VariantRepository variantRepository;
    private final BookVariantRepository bookVariantRepository;
    private final StockRequestRepository stockRequestRepository;
    private final NotificationService notificationService;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final EntityManager entityManager;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request) throws IdInvalidException {
        Supplier supplier = findSupplierById(request.getSupplierId());
        User createdBy = findUserById(request.getCreatedById());

        PurchaseOrder purchaseOrder = createPurchaseOrderFromRequest(request, supplier, createdBy);
        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        createPurchaseOrderItems(savedPurchaseOrder, request.getItems());
        
        entityManager.flush();
        entityManager.clear();

        PurchaseOrder purchaseOrderWithItems = findPurchaseOrderById(savedPurchaseOrder.getId());
        messagingTemplate.convertAndSend("/topic/purchase-orders",
                new WsEvent("CREATED", "PURCHASE_ORDER", purchaseOrderWithItems.getId(), null));
        notificationService.notifyAllByRole("ADMIN",
                "Don dat hang NCC #" + purchaseOrderWithItems.getId() + " moi",
                "Co don dat hang NCC moi can duyet");

        return purchaseOrderMapper.toPurchaseOrderResponse(purchaseOrderWithItems);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrderFromStockRequest(CreatePurchaseOrderFromStockRequestRequest request) throws IdInvalidException {
        // Lấy StockRequest đã duyệt
        StockRequest stockRequest = findStockRequestById(request.getStockRequestId());
        validateStockRequestStatusApproved(stockRequest);

        // Lấy thông tin Supplier và User
        Supplier supplier = findSupplierById(request.getSupplierId());
        User createdBy = findUserById(request.getCreatedById());

        // Tạo PurchaseOrder
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setCreatedBy(createdBy);
        purchaseOrder.setNote(request.getNote());
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        // Tạo PurchaseOrderItem từ StockRequest
        Book book = stockRequest.getBook();
        Variant variant = stockRequest.getVariant();

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setPurchaseOrder(savedPurchaseOrder);
        item.setBook(book);
        item.setVariant(variant);
        item.setQuantity(stockRequest.getQuantity());
        item.setImportPrice(request.getImportPrice());

        purchaseOrderItemRepository.save(item);

        // Cập nhật status StockRequest thành ORDERED
        stockRequest.setStatus(StockRequestStatus.ORDERED);
        stockRequestRepository.save(stockRequest);

        entityManager.flush();
        entityManager.clear();

        PurchaseOrder purchaseOrderWithItems = findPurchaseOrderById(savedPurchaseOrder.getId());
        messagingTemplate.convertAndSend("/topic/purchase-orders",
                new WsEvent("CREATED", "PURCHASE_ORDER", purchaseOrderWithItems.getId(), null));
        notificationService.notifyAllByRole("ADMIN",
                "Don dat hang NCC #" + purchaseOrderWithItems.getId() + " moi",
                "Co don dat hang NCC moi can duyet");
        return purchaseOrderMapper.toPurchaseOrderResponse(purchaseOrderWithItems);
    }

    private void validateStockRequestStatusApproved(StockRequest stockRequest) {
        if (stockRequest.getStatus() != StockRequestStatus.APPROVED) {
            throw new RuntimeException("Stock request must be APPROVED to create purchase order. Current status: " + stockRequest.getStatus());
        }
    }

    private StockRequest findStockRequestById(Long stockRequestId) {
        return stockRequestRepository.findById(stockRequestId)
                .orElseThrow(() -> new RuntimeException("Stock request not found with identifier: " + stockRequestId));
    }

    private void validatePurchaseOrderItems(List<CreatePurchaseOrderRequest.PurchaseOrderItemRequest> items) {
        for (CreatePurchaseOrderRequest.PurchaseOrderItemRequest item : items) {
            if (!bookVariantRepository.existsByBookIdAndVariantId(item.getBookId(), item.getVariantId())) {
                throw new RuntimeException("Variant " + item.getVariantId() + " does not belong to Book " + item.getBookId());
            }
        }
    }

    private Supplier findSupplierById(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found with identifier: " + supplierId));
    }

    private PurchaseOrder createPurchaseOrderFromRequest(CreatePurchaseOrderRequest request, Supplier supplier, User createdBy) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setCreatedBy(createdBy);
        purchaseOrder.setNote(request.getNote());
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING);
        return purchaseOrder;
    }

    private void createPurchaseOrderItems(PurchaseOrder purchaseOrder, List<CreatePurchaseOrderRequest.PurchaseOrderItemRequest> itemRequests) {
        validatePurchaseOrderItems(itemRequests);
        for (CreatePurchaseOrderRequest.PurchaseOrderItemRequest itemRequest : itemRequests) {
            Book book = findBookById(itemRequest.getBookId());
            Variant variant = findVariantById(itemRequest.getVariantId());

            //check if the book and variant are available
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(purchaseOrder);
            item.setBook(book);
            item.setVariant(variant);
            item.setQuantity(itemRequest.getQuantity());
            item.setImportPrice(itemRequest.getImportPrice());

            purchaseOrderItemRepository.save(item);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll();
        return mapToPurchaseOrderResponseList(purchaseOrders);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderResponse getPurchaseOrderById(Long id) throws IdInvalidException {
        PurchaseOrder purchaseOrder = findPurchaseOrderById(id);
        return purchaseOrderMapper.toPurchaseOrderResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public void deletePurchaseOrder(Long id) throws IdInvalidException {
        PurchaseOrder purchaseOrder = findPurchaseOrderById(id);
        validatePurchaseOrderCanBeDeleted(purchaseOrder);
        purchaseOrderRepository.delete(purchaseOrder);
    }

    private void validatePurchaseOrderCanBeDeleted(PurchaseOrder purchaseOrder) throws IdInvalidException {
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new IdInvalidException("Only PENDING purchase orders can be deleted");
        }
    }

    private PurchaseOrder findPurchaseOrderById(Long id) throws IdInvalidException {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Purchase order not found: " + id));
    }

    private List<PurchaseOrderResponse> mapToPurchaseOrderResponseList(List<PurchaseOrder> purchaseOrders) {
        return purchaseOrders.stream()
                .map(purchaseOrderMapper::toPurchaseOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PurchaseOrderResponse approvePurchaseOrder(Long purchaseOrderId, Long approvedById) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Duyệt đơn để có thể nhập hàng
        validatePurchaseOrderId(purchaseOrderId);
        validateApprovedById(approvedById);

        PurchaseOrder purchaseOrder = findPurchaseOrderById(purchaseOrderId);
        validatePurchaseOrderStatusForApproval(purchaseOrder);

        User approvedBy = findUserById(approvedById);
        approvePurchaseOrder(purchaseOrder, approvedBy);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        messagingTemplate.convertAndSend("/topic/purchase-orders",
                new WsEvent("UPDATED", "PURCHASE_ORDER", savedPurchaseOrder.getId(), null));
        notificationService.notifyAllByRole("WAREHOUSE_STAFF",
                "Don dat hang #" + savedPurchaseOrder.getId() + " duoc duyet",
                "Don dat hang moi duoc duyet, chuan bi nhan hang");
        return purchaseOrderMapper.toPurchaseOrderResponse(savedPurchaseOrder);
    }

    private void validatePurchaseOrderStatusForApproval(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new RuntimeException("Purchase order can only be approved when status is PENDING. Current status: " + purchaseOrder.getStatus());
        }
    }

    private void approvePurchaseOrder(PurchaseOrder purchaseOrder, User approvedBy) {
        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
        purchaseOrder.setApprovedAt(LocalDateTime.now());
        purchaseOrder.setApprovedBy(approvedBy);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse rejectPurchaseOrder(Long purchaseOrderId, Long approvedById) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Từ chối đơn, không cho nhập hàng
        validatePurchaseOrderId(purchaseOrderId);
        validateApprovedById(approvedById);

        PurchaseOrder purchaseOrder = findPurchaseOrderById(purchaseOrderId);
        validatePurchaseOrderStatusForRejection(purchaseOrder);

        User approvedBy = findUserById(approvedById);
        rejectPurchaseOrder(purchaseOrder, approvedBy);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        messagingTemplate.convertAndSend("/topic/purchase-orders",
                new WsEvent("UPDATED", "PURCHASE_ORDER", savedPurchaseOrder.getId(), null));
        notificationService.createNotification(null, savedPurchaseOrder.getCreatedBy(),
                "Don dat hang #" + savedPurchaseOrder.getId() + " bi tu choi",
                "Don dat hang NCC cua ban da bi tu choi");
        return purchaseOrderMapper.toPurchaseOrderResponse(savedPurchaseOrder);
    }

    private void validatePurchaseOrderStatusForRejection(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.PENDING) {
            throw new RuntimeException("Purchase order can only be rejected when status is PENDING. Current status: " + purchaseOrder.getStatus());
        }
    }

    private void rejectPurchaseOrder(PurchaseOrder purchaseOrder, User approvedBy) {
        purchaseOrder.setStatus(PurchaseOrderStatus.REJECTED);
        purchaseOrder.setApprovedAt(LocalDateTime.now());
        purchaseOrder.setApprovedBy(approvedBy);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse cancelPurchaseOrder(Long purchaseOrderId, Long cancelledById, String cancelReason) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Hủy đơn đã APPROVED do có vấn đề khi nhập hàng
        validatePurchaseOrderId(purchaseOrderId);
        validateApprovedById(cancelledById);
        validateCancelReason(cancelReason);

        PurchaseOrder purchaseOrder = findPurchaseOrderById(purchaseOrderId);
        validatePurchaseOrderStatusForCancellation(purchaseOrder);

        User cancelledBy = findUserById(cancelledById);
        cancelPurchaseOrder(purchaseOrder, cancelledBy, cancelReason);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        messagingTemplate.convertAndSend("/topic/purchase-orders",
                new WsEvent("UPDATED", "PURCHASE_ORDER", savedPurchaseOrder.getId(), null));
        return purchaseOrderMapper.toPurchaseOrderResponse(savedPurchaseOrder);
    }

    private void validatePurchaseOrderStatusForCancellation(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new RuntimeException("Purchase order can only be cancelled when status is APPROVED. Current status: " + purchaseOrder.getStatus());
        }
    }

    private void validateCancelReason(String cancelReason) throws IdInvalidException {
        if (cancelReason == null || cancelReason.trim().isEmpty()) {
            throw new IdInvalidException("Cancel reason is required and cannot be empty");
        }
    }

    private void cancelPurchaseOrder(PurchaseOrder purchaseOrder, User cancelledBy, String cancelReason) {
        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrder.setApprovedAt(LocalDateTime.now());
        purchaseOrder.setApprovedBy(cancelledBy);
        purchaseOrder.setCancelReason(cancelReason.trim());
    }

    @Override
    @Transactional
    public PurchaseOrderResponse payPurchaseOrder(Long purchaseOrderId, Long paidById) throws IdInvalidException {
        // Role: ADMIN - Thanh toán đơn đã APPROVED
        validatePurchaseOrderId(purchaseOrderId);
        validateApprovedById(paidById);

        PurchaseOrder purchaseOrder = findPurchaseOrderById(purchaseOrderId);
        validatePurchaseOrderStatusForPayment(purchaseOrder);

        User paidBy = findUserById(paidById);

        double totalAmount = calculateTotalAmount(purchaseOrder);
        payPurchaseOrder(purchaseOrder, paidBy, totalAmount);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        messagingTemplate.convertAndSend("/topic/purchase-orders",
                new WsEvent("UPDATED", "PURCHASE_ORDER", savedPurchaseOrder.getId(), null));
        return purchaseOrderMapper.toPurchaseOrderResponse(savedPurchaseOrder);
    }

    private void validatePurchaseOrderStatusForPayment(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new RuntimeException("Purchase order can only be paid when status is APPROVED. Current status: " + purchaseOrder.getStatus());
        }
    }

    private double calculateTotalAmount(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getPurchaseOrderItems() == null || purchaseOrder.getPurchaseOrderItems().isEmpty()) {
            throw new RuntimeException("Purchase order has no items to calculate total amount");
        }

        return purchaseOrder.getPurchaseOrderItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getImportPrice())
                .sum();
    }

    private void payPurchaseOrder(PurchaseOrder purchaseOrder, User paidBy, double totalAmount) {
        purchaseOrder.setStatus(PurchaseOrderStatus.ORDERED);
        // Không thay đổi approvedAt/approvedBy vì đây là thông tin của lần approve trước đó
        // TODO: Trừ tiền từ tài khoản nếu có hệ thống tài khoản
        // Hiện tại chỉ cập nhật status, chưa trừ tiền thực tế
        // totalAmount đã được tính để có thể sử dụng cho logic trừ tiền sau này
    }


    private void validatePurchaseOrderId(Long purchaseOrderId) throws IdInvalidException {
        if (purchaseOrderId == null || purchaseOrderId <= 0) {
            throw new IdInvalidException("Purchase order identifier is invalid: " + purchaseOrderId);
        }
    }

    private void validateApprovedById(Long approvedById) throws IdInvalidException {
        if (approvedById == null || approvedById <= 0) {
            throw new IdInvalidException("Approved by user identifier is invalid: " + approvedById);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + userId));
    }

    private Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));
    }

    private Variant findVariantById(Long variantId) {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found with identifier: " + variantId));
    }
}
