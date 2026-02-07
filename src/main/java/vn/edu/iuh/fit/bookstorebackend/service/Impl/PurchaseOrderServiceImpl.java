package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.common.PurchaseOrderStatus;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePurchaseOrderRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PurchaseOrderResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.PurchaseOrderMapper;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.PurchaseOrder;
import vn.edu.iuh.fit.bookstorebackend.model.PurchaseOrderItem;
import vn.edu.iuh.fit.bookstorebackend.model.Supplier;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.PurchaseOrderItemRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.PurchaseOrderRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.SupplierRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.PurchaseOrderService;

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
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request) throws IdInvalidException {
        // Role: ADMIN, WAREHOUSE_STAFF, hoặc SELLER - Tạo đơn đặt hàng từ nhà cung cấp
        validateCreatePurchaseOrderRequest(request);

        Supplier supplier = findSupplierById(request.getSupplierId());
        User createdBy = findUserById(request.getCreatedById());

        PurchaseOrder purchaseOrder = createPurchaseOrderFromRequest(request, supplier, createdBy);
        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        createPurchaseOrderItems(savedPurchaseOrder, request.getItems());
        
        entityManager.flush();
        entityManager.clear();

        PurchaseOrder purchaseOrderWithItems = findPurchaseOrderById(savedPurchaseOrder.getId());

        return purchaseOrderMapper.toPurchaseOrderResponse(purchaseOrderWithItems);
    }

    private void validateCreatePurchaseOrderRequest(CreatePurchaseOrderRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreatePurchaseOrderRequest cannot be null");
        }
        if (request.getSupplierId() == null || request.getSupplierId() <= 0) {
            throw new IdInvalidException("Supplier identifier is invalid");
        }
        if (request.getCreatedById() == null || request.getCreatedById() <= 0) {
            throw new IdInvalidException("User identifier is invalid");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IdInvalidException("Purchase order items cannot be null or empty");
        }
        for (CreatePurchaseOrderRequest.PurchaseOrderItemRequest item : request.getItems()) {
            if (item.getBookId() == null || item.getBookId() <= 0) {
                throw new IdInvalidException("Book identifier is invalid");
            }
            if (item.getQuantity() <= 0) {
                throw new IdInvalidException("Quantity must be greater than 0");
            }
            if (item.getImportPrice() < 0) {
                throw new IdInvalidException("Import price cannot be negative");
            }
        }
    }

    private Supplier findSupplierById(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found with identifier: " + supplierId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + userId));
    }

    private PurchaseOrder createPurchaseOrderFromRequest(CreatePurchaseOrderRequest request, Supplier supplier, User createdBy) {
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setCreatedBy(createdBy);
        purchaseOrder.setNote(request.getNote());
        purchaseOrder.setStatus(PurchaseOrderStatus.DRAFT);
        return purchaseOrder;
    }

    private void createPurchaseOrderItems(PurchaseOrder purchaseOrder, List<CreatePurchaseOrderRequest.PurchaseOrderItemRequest> itemRequests) {
        for (CreatePurchaseOrderRequest.PurchaseOrderItemRequest itemRequest : itemRequests) {
            Book book = findBookById(itemRequest.getBookId());

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(purchaseOrder);
            item.setBook(book);
            item.setQuantity(itemRequest.getQuantity());
            item.setImportPrice(itemRequest.getImportPrice());

            purchaseOrderItemRepository.save(item);
        }
    }

    private Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> getAllPurchaseOrders() {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll();
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
        validateApproverRole(approvedBy);
        approvePurchaseOrder(purchaseOrder, approvedBy);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        return purchaseOrderMapper.toPurchaseOrderResponse(savedPurchaseOrder);
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

    private void validateApproverRole(User approvedBy) {
        if (approvedBy.getRoles() == null || approvedBy.getRoles().isEmpty()) {
            throw new RuntimeException("User does not have any roles. Required roles: ADMIN or WAREHOUSE_STAFF");
        }

        boolean hasPermission = approvedBy.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getCode()) 
                        || "WAREHOUSE_STAFF".equals(role.getCode()));

        if (!hasPermission) {
            throw new RuntimeException("User does not have permission to approve/reject purchase orders. Required roles: ADMIN or WAREHOUSE_STAFF");
        }
    }

    private PurchaseOrder findPurchaseOrderById(Long purchaseOrderId) {
        return purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with identifier: " + purchaseOrderId));
    }

    private void validatePurchaseOrderStatusForApproval(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new RuntimeException("Purchase order can only be approved when status is DRAFT. Current status: " + purchaseOrder.getStatus());
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
        validateApproverRole(approvedBy);
        rejectPurchaseOrder(purchaseOrder, approvedBy);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        return purchaseOrderMapper.toPurchaseOrderResponse(savedPurchaseOrder);
    }

    private void validatePurchaseOrderStatusForRejection(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new RuntimeException("Purchase order can only be rejected when status is DRAFT. Current status: " + purchaseOrder.getStatus());
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
        validateCancelReason(cancelReason); // Validate lý do hủy bắt buộc

        PurchaseOrder purchaseOrder = findPurchaseOrderById(purchaseOrderId);
        validatePurchaseOrderStatusForCancellation(purchaseOrder);

        User cancelledBy = findUserById(cancelledById);
        validateApproverRole(cancelledBy);
        cancelPurchaseOrder(purchaseOrder, cancelledBy, cancelReason);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);
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
        purchaseOrder.setCancelReason(cancelReason.trim()); // Lưu lý do hủy
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
        validateAdminRole(paidBy);

        // Tính tổng tiền từ PurchaseOrderItems
        double totalAmount = calculateTotalAmount(purchaseOrder);
        
        // Thanh toán: Set status = ORDERED
        payPurchaseOrder(purchaseOrder, paidBy, totalAmount);

        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        return purchaseOrderMapper.toPurchaseOrderResponse(savedPurchaseOrder);
    }

    private void validatePurchaseOrderStatusForPayment(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new RuntimeException("Purchase order can only be paid when status is APPROVED. Current status: " + purchaseOrder.getStatus());
        }
    }

    private void validateAdminRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new RuntimeException("User does not have any roles. Required role: ADMIN");
        }

        boolean hasPermission = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getCode()));

        if (!hasPermission) {
            throw new RuntimeException("User does not have permission to pay purchase order. Required role: ADMIN");
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
}
