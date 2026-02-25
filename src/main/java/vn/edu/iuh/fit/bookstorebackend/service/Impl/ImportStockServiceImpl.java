package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateImportStockRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ImportStockResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.ImportStockMapper;
import vn.edu.iuh.fit.bookstorebackend.common.PurchaseOrderStatus;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.BookVariant;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStock;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStockDetail;
import vn.edu.iuh.fit.bookstorebackend.model.PurchaseOrder;
import vn.edu.iuh.fit.bookstorebackend.model.PurchaseOrderItem;
import vn.edu.iuh.fit.bookstorebackend.model.Supplier;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.BookVariantRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.ImportStockDetailRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.ImportStockRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.PurchaseOrderRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.SupplierRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.VariantRepository;
import vn.edu.iuh.fit.bookstorebackend.service.ImportStockService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImportStockServiceImpl implements ImportStockService {

    private final ImportStockRepository importStockRepository;
    private final ImportStockDetailRepository importStockDetailRepository;
    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookVariantRepository bookVariantRepository;
    private final VariantRepository variantRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ImportStockMapper importStockMapper;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public ImportStockResponse createImportStock(CreateImportStockRequest request) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Tạo phiếu nhập kho từ PurchaseOrder đã APPROVED
        validateCreateImportStockRequest(request);

        Supplier supplier = findSupplierById(request.getSupplierId());
        User createdBy = findUserById(request.getCreatedById());
        validateImporterRole(createdBy);
        
        PurchaseOrder purchaseOrder = findPurchaseOrderById(request.getPurchaseOrderId());
        validatePurchaseOrderForImport(purchaseOrder);

        ImportStock importStock = createImportStockFromRequest(request, supplier, createdBy, purchaseOrder);
        ImportStock savedImportStock = importStockRepository.save(importStock);

        createImportStockDetails(savedImportStock, request.getDetails(), purchaseOrder);

        entityManager.flush();
        entityManager.clear();

        ImportStock importStockWithDetails = findImportStockById(savedImportStock.getId());

        return importStockMapper.toImportStockResponse(importStockWithDetails);
    }

    private void validateCreateImportStockRequest(CreateImportStockRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateImportStockRequest cannot be null");
        }
        if (request.getSupplierId() == null || request.getSupplierId() <= 0) {
            throw new IdInvalidException("Supplier identifier is invalid");
        }
        if (request.getCreatedById() == null || request.getCreatedById() <= 0) {
            throw new IdInvalidException("User identifier is invalid");
        }
        if (request.getPurchaseOrderId() == null || request.getPurchaseOrderId() <= 0) {
            throw new IdInvalidException("Purchase order identifier is invalid");
        }
        // Details optional: nếu null/empty, sẽ lấy từ PurchaseOrder
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            for (CreateImportStockRequest.ImportStockDetailRequest detail : request.getDetails()) {
                if (detail.getBookId() == null || detail.getBookId() <= 0) {
                    throw new IdInvalidException("Book identifier is invalid");
                }
                if (detail.getVariantId() == null || detail.getVariantId() <= 0) {
                    throw new IdInvalidException("Variant identifier is invalid");
                }
                if (detail.getQuantity() <= 0) {
                    throw new IdInvalidException("Quantity must be greater than 0");
                }
                if (detail.getImportPrice() < 0) {
                    throw new IdInvalidException("Import price cannot be negative");
                }
            }
        }
    }

    private Supplier findSupplierById(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found with identifier: " + supplierId));
    }

    private void validateImporterRole(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            throw new RuntimeException("User does not have any roles. Required roles: ADMIN or WAREHOUSE_STAFF");
        }

        boolean hasPermission = user.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getCode()) 
                        || "WAREHOUSE_STAFF".equals(role.getCode()));

        if (!hasPermission) {
            throw new RuntimeException("User does not have permission to create import stock. Required roles: ADMIN or WAREHOUSE_STAFF");
        }
    }

    private void validatePurchaseOrderForImport(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.APPROVED 
                && purchaseOrder.getStatus() != PurchaseOrderStatus.ORDERED) {
            throw new RuntimeException("Purchase order must be APPROVED or ORDERED to create import stock. Current status: " + purchaseOrder.getStatus());
        }
    }

    private ImportStock createImportStockFromRequest(CreateImportStockRequest request, Supplier supplier, User createdBy, PurchaseOrder purchaseOrder) {
        ImportStock importStock = new ImportStock();
        importStock.setImportDate(LocalDateTime.now());
        importStock.setSupplier(supplier);
        importStock.setCreatedBy(createdBy);
        importStock.setPurchaseOrder(purchaseOrder);
        return importStock;
    }

    private void createImportStockDetails(ImportStock importStock, List<CreateImportStockRequest.ImportStockDetailRequest> detailRequests, PurchaseOrder purchaseOrder) {
        // Nếu details null/empty, lấy từ PurchaseOrder
        if (detailRequests == null || detailRequests.isEmpty()) {
            List<PurchaseOrderItem> poItems = purchaseOrder.getPurchaseOrderItems();
            if (poItems == null || poItems.isEmpty()) {
                throw new RuntimeException("Purchase order has no items");
            }
            for (PurchaseOrderItem poItem : poItems) {
                createImportStockDetailFromPOItem(importStock, poItem);
            }
        } else {
            for (CreateImportStockRequest.ImportStockDetailRequest detailRequest : detailRequests) {
                createImportStockDetailFromRequest(importStock, detailRequest);
            }
        }
    }

    private void createImportStockDetailFromPOItem(ImportStock importStock, PurchaseOrderItem poItem) {
        Book book = poItem.getBook();
        Variant variant = poItem.getVariant();

        ImportStockDetail detail = new ImportStockDetail();
        detail.setImportStock(importStock);
        detail.setBook(book);
        detail.setVariant(variant);
        detail.setQuantity(poItem.getQuantity());
        detail.setImportPrice(poItem.getImportPrice());

        importStockDetailRepository.save(detail);

        updateStockQuantity(book, variant, poItem.getQuantity());
    }

    private void createImportStockDetailFromRequest(ImportStock importStock, CreateImportStockRequest.ImportStockDetailRequest detailRequest) {
        Book book = findBookById(detailRequest.getBookId());
        Variant variant = findVariantById(detailRequest.getVariantId());

        ImportStockDetail detail = new ImportStockDetail();
        detail.setImportStock(importStock);
        detail.setBook(book);
        detail.setVariant(variant);
        detail.setQuantity(detailRequest.getQuantity());
        detail.setImportPrice(detailRequest.getImportPrice());

        importStockDetailRepository.save(detail);

        updateStockQuantity(book, variant, detailRequest.getQuantity());
    }

    // Updated: now also updates bookVariant stock
    private void updateStockQuantity(Book book, Variant variant, int quantity) {
        // Update book total stock
        book.setStockQuantity(book.getStockQuantity() + quantity);
        bookRepository.save(book);

        // Update bookVariant stock
        BookVariant bookVariant = bookVariantRepository.findByBookIdAndVariantId(book.getId(), variant.getId())
                .orElseThrow(() -> new RuntimeException("BookVariant not found for book: " + book.getId() + " and variant: " + variant.getId()));
        bookVariant.setStockQuantity(bookVariant.getStockQuantity() + quantity);
        bookVariantRepository.save(bookVariant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportStockResponse> getAllImportStocks() {
        List<ImportStock> importStocks = importStockRepository.findAll();
        return mapToImportStockResponseList(importStocks);
    }

    private List<ImportStockResponse> mapToImportStockResponseList(List<ImportStock> importStocks) {
        return importStocks.stream()
                .map(importStockMapper::toImportStockResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportStockResponse> getImportHistoryByBookId(Long bookId) throws IdInvalidException {
        validateBookId(bookId);
        validateBookExists(bookId);

        List<ImportStock> importStocks = importStockDetailRepository.findImportStocksByBookId(bookId);
        return mapToImportStockResponseList(importStocks);
    }

    private void validateBookId(Long bookId) throws IdInvalidException {
        if (bookId == null || bookId <= 0) {
            throw new IdInvalidException("Book identifier is invalid: " + bookId);
        }
    }

    private void validateBookExists(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("Book not found with identifier: " + bookId);
        }
    }


    private ImportStock findImportStockById(Long importStockId) {
        return importStockRepository.findById(importStockId)
                .orElseThrow(() -> new RuntimeException("Import stock not found with identifier: " + importStockId));
    }

    private PurchaseOrder findPurchaseOrderById(Long purchaseOrderId) {
        return purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with identifier: " + purchaseOrderId));
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
