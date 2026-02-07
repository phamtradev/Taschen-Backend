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
import vn.edu.iuh.fit.bookstorebackend.model.ImportStock;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStockDetail;
import vn.edu.iuh.fit.bookstorebackend.model.PurchaseOrder;
import vn.edu.iuh.fit.bookstorebackend.model.Supplier;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.ImportStockDetailRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.ImportStockRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.PurchaseOrderRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.SupplierRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
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
        
        // Validate role: chỉ ADMIN hoặc WAREHOUSE_STAFF mới được tạo ImportStock
        validateImporterRole(createdBy);
        
        // Validate PurchaseOrder: phải tồn tại và status = APPROVED
        PurchaseOrder purchaseOrder = findPurchaseOrderById(request.getPurchaseOrderId());
        validatePurchaseOrderForImport(purchaseOrder);

        ImportStock importStock = createImportStockFromRequest(request, supplier, createdBy, purchaseOrder);
        ImportStock savedImportStock = importStockRepository.save(importStock);

        createImportStockDetails(savedImportStock, request.getDetails());

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
        if (request.getDetails() == null || request.getDetails().isEmpty()) {
            throw new IdInvalidException("Import stock details cannot be null or empty");
        }
        for (CreateImportStockRequest.ImportStockDetailRequest detail : request.getDetails()) {
            if (detail.getBookId() == null || detail.getBookId() <= 0) {
                throw new IdInvalidException("Book identifier is invalid");
            }
            if (detail.getQuantity() <= 0) {
                throw new IdInvalidException("Quantity must be greater than 0");
            }
            if (detail.getImportPrice() < 0) {
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

    private ImportStock createImportStockFromRequest(CreateImportStockRequest request, Supplier supplier, User createdBy, PurchaseOrder purchaseOrder) {
        ImportStock importStock = new ImportStock();
        importStock.setImportDate(LocalDateTime.now());
        importStock.setSupplier(supplier);
        importStock.setCreatedBy(createdBy);
        importStock.setPurchaseOrder(purchaseOrder);
        return importStock;
    }
    
    private PurchaseOrder findPurchaseOrderById(Long purchaseOrderId) {
        return purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with identifier: " + purchaseOrderId));
    }
    
    private ImportStock findImportStockById(Long importStockId) {
        return importStockRepository.findById(importStockId)
                .orElseThrow(() -> new RuntimeException("Import stock not found with identifier: " + importStockId));
    }
    
    private void validatePurchaseOrderForImport(PurchaseOrder purchaseOrder) {
        // Cho phép nhập hàng khi status = APPROVED hoặc ORDERED (nhập từng phần, có thể thanh toán trước)
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.APPROVED 
                && purchaseOrder.getStatus() != PurchaseOrderStatus.ORDERED) {
            throw new RuntimeException("Purchase order must be APPROVED or ORDERED to create import stock. Current status: " + purchaseOrder.getStatus());
        }
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

    private void createImportStockDetails(ImportStock importStock, List<CreateImportStockRequest.ImportStockDetailRequest> detailRequests) {
        for (CreateImportStockRequest.ImportStockDetailRequest detailRequest : detailRequests) {
            Book book = findBookById(detailRequest.getBookId());

            ImportStockDetail detail = new ImportStockDetail();
            detail.setImportStock(importStock);
            detail.setBook(book);
            detail.setQuantity(detailRequest.getQuantity());
            detail.setImportPrice(detailRequest.getImportPrice());

            importStockDetailRepository.save(detail);

            updateBookStockQuantity(book, detailRequest.getQuantity());
        }
    }

    private Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));
    }

    private void updateBookStockQuantity(Book book, int quantity) {
        book.setStockQuantity(book.getStockQuantity() + quantity);
        bookRepository.save(book);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImportStockResponse> getAllImportStocks() {
        List<ImportStock> importStocks = importStockRepository.findAll();
        return mapToImportStockResponseList(importStocks);
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

    private List<ImportStockResponse> mapToImportStockResponseList(List<ImportStock> importStocks) {
        return importStocks.stream()
                .map(importStockMapper::toImportStockResponse)
                .collect(Collectors.toList());
    }
}
