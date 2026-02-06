package vn.edu.iuh.fit.bookstorebackend.service.Impl;

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

    @Override
    @Transactional
    public PurchaseOrderResponse createPurchaseOrder(CreatePurchaseOrderRequest request) throws IdInvalidException {
        validateCreatePurchaseOrderRequest(request);

        Supplier supplier = findSupplierById(request.getSupplierId());
        User createdBy = findUserById(request.getCreatedById());

        PurchaseOrder purchaseOrder = createPurchaseOrderFromRequest(request, supplier, createdBy);
        PurchaseOrder savedPurchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        createPurchaseOrderItems(savedPurchaseOrder, request.getItems());

        return purchaseOrderMapper.toPurchaseOrderResponse(savedPurchaseOrder);
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
        validatePurchaseOrderId(purchaseOrderId);
        validateApprovedById(approvedById);

        PurchaseOrder purchaseOrder = findPurchaseOrderById(purchaseOrderId);
        validatePurchaseOrderStatusForApproval(purchaseOrder);

        User approvedBy = findUserById(approvedById);
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
        validatePurchaseOrderId(purchaseOrderId);
        validateApprovedById(approvedById);

        PurchaseOrder purchaseOrder = findPurchaseOrderById(purchaseOrderId);
        validatePurchaseOrderStatusForRejection(purchaseOrder);

        User approvedBy = findUserById(approvedById);
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
}
