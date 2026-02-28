package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateImportStockRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ImportStockResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ReceiveStockResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.ImportStockMapper;
import vn.edu.iuh.fit.bookstorebackend.common.PurchaseOrderStatus;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.BookVariant;
import vn.edu.iuh.fit.bookstorebackend.model.Batch;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStock;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStockDetail;
import vn.edu.iuh.fit.bookstorebackend.model.PurchaseOrder;
import vn.edu.iuh.fit.bookstorebackend.model.PurchaseOrderItem;
import vn.edu.iuh.fit.bookstorebackend.model.Supplier;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.BookVariantRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.BatchRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.ImportStockDetailRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.ImportStockRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.PurchaseOrderRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.SupplierRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.VariantRepository;
import vn.edu.iuh.fit.bookstorebackend.service.ImportStockService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    private final BatchRepository batchRepository;
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
        validateSupplierMatchesPurchaseOrder(request.getSupplierId(), purchaseOrder);

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
                if (detail.getSupplierId() == null || detail.getSupplierId() <= 0) {
                    throw new IdInvalidException("Supplier identifier is invalid");
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

    private void validateSupplierMatchesPurchaseOrder(Long supplierId, PurchaseOrder purchaseOrder) throws IdInvalidException {
        if (purchaseOrder.getSupplier() == null) {
            throw new RuntimeException("Purchase order has no supplier");
        }
        if (!purchaseOrder.getSupplier().getId().equals(supplierId)) {
            throw new IdInvalidException("Supplier must match the supplier in purchase order: " + purchaseOrder.getSupplier().getName());
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
        Supplier supplier = importStock.getSupplier();

        ImportStockDetail detail = new ImportStockDetail();
        detail.setImportStock(importStock);
        detail.setBook(book);
        detail.setVariant(variant);
        detail.setQuantity(poItem.getQuantity());
        detail.setImportPrice(poItem.getImportPrice());
        detail.setSupplier(supplier);

        importStockDetailRepository.save(detail);
        // KHÔNG tạo Batch ở đây nữa - sẽ tạo khi "Nhập kho"
    }

    private void createImportStockDetailFromRequest(ImportStock importStock, CreateImportStockRequest.ImportStockDetailRequest detailRequest) {
        Book book = findBookById(detailRequest.getBookId());
        Variant variant = findVariantById(detailRequest.getVariantId());
        Supplier supplier = findSupplierById(detailRequest.getSupplierId());

        ImportStockDetail detail = new ImportStockDetail();
        detail.setImportStock(importStock);
        detail.setBook(book);
        detail.setVariant(variant);
        detail.setQuantity(detailRequest.getQuantity());
        detail.setImportPrice(detailRequest.getImportPrice());
        detail.setSupplier(supplier);

        importStockDetailRepository.save(detail);        
    }

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

    /**
     * Nhập kho: tạo Batch từ ImportStockDetail
     * - Nếu batch đã tồn tại (cùng book + variant + importPrice + supplier) → cộng dồn số lượng
     * - Nếu batch mới → tạo batch mới
     */
    @Override
    @Transactional
    public ReceiveStockResponse receiveStock(Long importStockId, Long userId) throws IdInvalidException {
        validateReceiveStockRequest(importStockId, userId);

        ImportStock importStock = findImportStockById(importStockId);
        validateImportStockNotReceived(importStock);

        User createdBy = findUserById(userId);
        validateImporterRole(createdBy);

        List<ImportStockDetail> details = getImportStockDetails(importStock);

        List<ReceiveStockResponse.BatchReceiveResult> batchResults = processBatchCreation(details, createdBy);

        markAsReceived(importStock);

        return buildReceiveStockResponse(importStockId, batchResults);
    }

    private void validateReceiveStockRequest(Long importStockId, Long userId) throws IdInvalidException {
        if (importStockId == null || importStockId <= 0) {
            throw new IdInvalidException("Import stock identifier is invalid");
        }
        if (userId == null || userId <= 0) {
            throw new IdInvalidException("User identifier is invalid");
        }
    }

    private void validateImportStockNotReceived(ImportStock importStock) {
        if (importStock.isReceived()) {
            throw new RuntimeException("Import stock has already been received");
        }
    }

    private List<ImportStockDetail> getImportStockDetails(ImportStock importStock) {
        List<ImportStockDetail> details = importStock.getImportStockDetails();
        if (details == null || details.isEmpty()) {
            throw new RuntimeException("Import stock has no details");
        }
        return details;
    }

    private List<ReceiveStockResponse.BatchReceiveResult> processBatchCreation(List<ImportStockDetail> details, User createdBy) {
        List<ReceiveStockResponse.BatchReceiveResult> batchResults = new java.util.ArrayList<>();
        for (ImportStockDetail detail : details) {
            ReceiveStockResponse.BatchReceiveResult result = createBatchFromDetail(detail, createdBy);
            batchResults.add(result);
            updateStockQuantity(detail.getBook(), detail.getVariant(), detail.getQuantity());
        }
        return batchResults;
    }

    private void markAsReceived(ImportStock importStock) {
        importStock.setReceived(true);
        importStockRepository.save(importStock);
    }

    private ReceiveStockResponse buildReceiveStockResponse(Long importStockId, List<ReceiveStockResponse.BatchReceiveResult> batchResults) {
        return ReceiveStockResponse.builder()
                .importStockId(importStockId)
                .received(true)
                .batchResults(batchResults)
                .build();
    }

    private ReceiveStockResponse.BatchReceiveResult createBatchFromDetail(ImportStockDetail detail, User createdBy) {
        Book book = detail.getBook();
        Variant variant = detail.getVariant();
        Supplier supplier = detail.getSupplier();
        int quantity = detail.getQuantity();
        double importPrice = detail.getImportPrice();

        var existingBatch = findExistingBatch(book, variant, importPrice, supplier);

        if (existingBatch.isPresent()) {
            return mergeBatch(existingBatch.get(), detail, quantity);
        } else {
            return createNewBatch(detail, createdBy, quantity, importPrice);
        }
    }

    private Optional<Batch> findExistingBatch(Book book, Variant variant, double importPrice, Supplier supplier) {
        return batchRepository.findByBook_IdAndVariant_IdAndImportPriceAndSupplier_Id(
                book.getId(), variant.getId(), importPrice, supplier.getId());
    }

    private ReceiveStockResponse.BatchReceiveResult mergeBatch(Batch batch, ImportStockDetail detail, int quantity) {
        batch.setQuantity(batch.getQuantity() + quantity);
        batch.setRemainingQuantity(batch.getRemainingQuantity() + quantity);
        batch.setImportStockDetail(detail);
        batchRepository.save(batch);

        return buildBatchResponse(batch, detail.getBook(), detail.getVariant(), detail.getQuantity(), detail.getImportPrice(), false);
    }

    private ReceiveStockResponse.BatchReceiveResult createNewBatch(ImportStockDetail detail, User createdBy, int quantity, double importPrice) {
        Batch batch = createBatchEntity(detail, createdBy, quantity, importPrice);
        batchRepository.save(batch);

        return buildBatchResponse(batch, detail.getBook(), detail.getVariant(), quantity, importPrice, true);
    }

    private Batch createBatchEntity(ImportStockDetail detail, User createdBy, int quantity, double importPrice) {
        Batch batch = new Batch();
        batch.setBatchCode(generateBatchCode());
        batch.setQuantity(quantity);
        batch.setRemainingQuantity(quantity);
        batch.setImportPrice(importPrice);
        batch.setSupplier(detail.getSupplier());
        batch.setBook(detail.getBook());
        batch.setCreatedBy(createdBy);
        batch.setVariant(detail.getVariant());
        batch.setImportStockDetail(detail);
        batch.setCreatedAt(LocalDateTime.now());
        return batch;
    }

    private ReceiveStockResponse.BatchReceiveResult buildBatchResponse(Batch batch, Book book, Variant variant, int quantity, double importPrice, boolean isNew) {
        return ReceiveStockResponse.BatchReceiveResult.builder()
                .batchId(batch.getId())
                .batchCode(batch.getBatchCode())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .variantId(variant.getId())
                .variantName(variant.getFormatName())
                .quantity(quantity)
                .importPrice(importPrice)
                .isNew(isNew)
                .build();
    }

    private String generateBatchCode() {
        String prefix = "LH";
        String month = String.format("%02d", LocalDateTime.now().getMonthValue());
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = batchRepository.count() + 1;
        return String.format("%s-%s-%s-%04d", prefix, month, year, count);
    }
}
