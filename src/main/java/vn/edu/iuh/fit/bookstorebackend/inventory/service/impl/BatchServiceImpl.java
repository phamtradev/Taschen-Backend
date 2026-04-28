package vn.edu.iuh.fit.bookstorebackend.inventory.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.CreateBatchDetailRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.request.CreateBatchRequest;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.response.BatchDetailResponse;
import vn.edu.iuh.fit.bookstorebackend.inventory.dto.response.BatchResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.inventory.mapper.BatchMapper;
import vn.edu.iuh.fit.bookstorebackend.inventory.model.Batch;
import vn.edu.iuh.fit.bookstorebackend.inventory.model.BatchDetail;
import vn.edu.iuh.fit.bookstorebackend.book.model.Book;
import vn.edu.iuh.fit.bookstorebackend.book.model.BookVariant;
import vn.edu.iuh.fit.bookstorebackend.inventory.model.ImportStockDetail;
import vn.edu.iuh.fit.bookstorebackend.order.model.OrderDetail;
import vn.edu.iuh.fit.bookstorebackend.supplier.model.Supplier;
import vn.edu.iuh.fit.bookstorebackend.user.model.User;
import vn.edu.iuh.fit.bookstorebackend.book.model.Variant;
import vn.edu.iuh.fit.bookstorebackend.inventory.repository.BatchDetailRepository;
import vn.edu.iuh.fit.bookstorebackend.inventory.repository.BatchRepository;
import vn.edu.iuh.fit.bookstorebackend.book.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.book.repository.BookVariantRepository;
import vn.edu.iuh.fit.bookstorebackend.inventory.repository.ImportStockDetailRepository;
import vn.edu.iuh.fit.bookstorebackend.order.repository.OrderDetailRepository;
import vn.edu.iuh.fit.bookstorebackend.supplier.repository.SupplierRepository;
import vn.edu.iuh.fit.bookstorebackend.user.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.book.repository.VariantRepository;
import vn.edu.iuh.fit.bookstorebackend.inventory.service.BatchService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final BatchDetailRepository batchDetailRepository;
    private final BookRepository bookRepository;
    private final BookVariantRepository bookVariantRepository;
    private final VariantRepository variantRepository;
    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final ImportStockDetailRepository importStockDetailRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final BatchMapper batchMapper;

    @Override
    @Transactional
    public BatchResponse createBatch(CreateBatchRequest request) throws IdInvalidException {
        Book book = findBookById(request.getBookId());
        User createdBy = findUserById(request.getCreatedById());

        Variant variant = findVariantById(request.getVariantId());

        Supplier supplier = findSupplierById(request.getSupplierId());

        ImportStockDetail importStockDetail = null;
        if (request.getImportStockDetailId() != null) {
            importStockDetail = findImportStockDetailById(request.getImportStockDetailId());
        }

        String batchCode = request.getBatchCode() != null && !request.getBatchCode().isEmpty()
                ? request.getBatchCode()
                : generateBatchCode();

        Batch batch = createBatchFromRequest(request, book, createdBy, variant, supplier, importStockDetail, batchCode);
        Batch savedBatch = batchRepository.save(batch);

        syncBookStockQuantity(book.getId());

        return batchMapper.toBatchResponse(savedBatch);
    }

    private String generateBatchCode() {
        String prefix = "LH";
        String month = String.format("%02d", LocalDateTime.now().getMonthValue());
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = batchRepository.count() + 1;
        return String.format("%s-%s-%s-%04d", prefix, month, year, count);
    }

    private Batch createBatchFromRequest(CreateBatchRequest request, Book book, User createdBy, Variant variant, Supplier supplier, ImportStockDetail importStockDetail, String batchCode) {
        Batch batch = new Batch();
        batch.setBatchCode(batchCode);
        batch.setQuantity(request.getQuantity());
        batch.setRemainingQuantity(request.getQuantity());
        batch.setImportPrice(request.getImportPrice());
        batch.setProductionDate(request.getProductionDate());
        batch.setCreatedAt(LocalDateTime.now());
        batch.setSupplier(supplier);
        batch.setBook(book);
        batch.setCreatedBy(createdBy);
        batch.setVariant(variant);
        batch.setImportStockDetail(importStockDetail);
        return batch;
    }

    @Override
    @Transactional(readOnly = true)
    public BatchResponse getBatchById(Long batchId) throws IdInvalidException {
        validateBatchId(batchId);
        Batch batch = findBatchById(batchId);
        BatchResponse response = batchMapper.toBatchResponse(batch);
        return enrichSingleWithSellingPrice(response, batch);
    }

    private void validateBatchId(Long batchId) throws IdInvalidException {
        if (batchId == null || batchId <= 0) {
            throw new IdInvalidException("Batch identifier is invalid: " + batchId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> getAllBatches() {
        List<Batch> batches = batchRepository.findAll();
        List<BatchResponse> responses = batchMapper.toBatchResponseList(batches);
        return enrichWithSellingPrice(responses, batches);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesByBookId(Long bookId) throws IdInvalidException {
        validateBookId(bookId);
        validateBookExists(bookId);
        List<Batch> batches = batchRepository.findByBook_IdOrderByCreatedAtDesc(bookId);
        List<BatchResponse> responses = batchMapper.toBatchResponseList(batches);
        return enrichWithSellingPrice(responses, batches);
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

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesByUserId(Long userId) throws IdInvalidException {
        validateUserId(userId);
        validateUserExists(userId);
        List<Batch> batches = batchRepository.findByCreatedBy_IdOrderByCreatedAtDesc(userId);
        List<BatchResponse> responses = batchMapper.toBatchResponseList(batches);
        return enrichWithSellingPrice(responses, batches);
    }

    private void validateUserId(Long userId) throws IdInvalidException {
        if (userId == null || userId <= 0) {
            throw new IdInvalidException("User identifier is invalid: " + userId);
        }
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with identifier: " + userId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> getAvailableBatchesByBookId(Long bookId) throws IdInvalidException {
        validateBookId(bookId);
        validateBookExists(bookId);
        List<Batch> batches = batchRepository.findByBook_IdAndRemainingQuantityGreaterThanOrderByCreatedAtAsc(bookId, 0);
        List<BatchResponse> responses = batchMapper.toBatchResponseList(batches);
        return enrichWithSellingPrice(responses, batches);
    }

    @Override
    @Transactional
    public BatchDetailResponse createBatchDetail(CreateBatchDetailRequest request) throws IdInvalidException {
        Batch batch = findBatchById(request.getBatchId());
        OrderDetail orderDetail = findOrderDetailById(request.getOrderDetailId());
        validateBatchAvailability(batch, request.getQuantity());
        BatchDetail batchDetail = createBatchDetailFromRequest(request, batch, orderDetail);
        BatchDetail savedBatchDetail = batchDetailRepository.save(batchDetail);
        updateBatchRemainingQuantity(batch.getId(), request.getQuantity());
        return batchMapper.toBatchDetailResponse(savedBatchDetail);
    }

    private void validateBatchAvailability(Batch batch, int quantity) {
        if (batch.getRemainingQuantity() < quantity) {
            throw new IllegalStateException("Not enough remaining quantity in batch. Available: " + batch.getRemainingQuantity() + ", Requested: " + quantity);
        }
    }

    private BatchDetail createBatchDetailFromRequest(CreateBatchDetailRequest request, Batch batch, OrderDetail orderDetail) {
        BatchDetail batchDetail = new BatchDetail();
        batchDetail.setBatch(batch);
        batchDetail.setOrderDetail(orderDetail);
        batchDetail.setQuantity(request.getQuantity());
        return batchDetail;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchDetailResponse> getBatchDetailsByOrderId(Long orderId) throws IdInvalidException {
        validateOrderId(orderId);
        List<BatchDetail> batchDetails = batchDetailRepository.findByOrderDetail_Order_Id(orderId);
        return batchMapper.toBatchDetailResponseList(batchDetails);
    }

    private void validateOrderId(Long orderId) throws IdInvalidException {
        if (orderId == null || orderId <= 0) {
            throw new IdInvalidException("Order identifier is invalid: " + orderId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchDetailResponse> getBatchDetailsByBatchId(Long batchId) throws IdInvalidException {
        validateBatchId(batchId);
        List<BatchDetail> batchDetails = batchDetailRepository.findByBatch_IdOrderByIdDesc(batchId);
        return batchMapper.toBatchDetailResponseList(batchDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchDetailResponse> getBatchDetailsByOrderDetailId(Long orderDetailId) throws IdInvalidException {
        validateOrderDetailId(orderDetailId);
        List<BatchDetail> batchDetails = batchDetailRepository.findByOrderDetail_Id(orderDetailId);
        return batchMapper.toBatchDetailResponseList(batchDetails);
    }

    private void validateOrderDetailId(Long orderDetailId) throws IdInvalidException {
        if (orderDetailId == null || orderDetailId <= 0) {
            throw new IdInvalidException("OrderDetail identifier is invalid: " + orderDetailId);
        }
    }

    @Override
    @Transactional
    public void updateBatchRemainingQuantity(Long batchId, int quantity) throws IdInvalidException {
        validateBatchId(batchId);
        Batch batch = findBatchById(batchId);
        if (batch.getRemainingQuantity() < quantity) {
            throw new IllegalStateException("Not enough remaining quantity. Available: " + batch.getRemainingQuantity() + ", Requested: " + quantity);
        }
        batch.setRemainingQuantity(batch.getRemainingQuantity() - quantity);
        batchRepository.save(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public Batch getBatchForSale(Long bookId, int quantity, String strategy) throws IdInvalidException {
        validateBookId(bookId);
        validateBookExists(bookId);

        List<Batch> availableBatches;

        switch (strategy != null ? strategy.toUpperCase() : "FIFO") {
            case "LIFO":
                availableBatches = batchRepository.findByBook_IdAndRemainingQuantityGreaterThanOrderByCreatedAtDesc(bookId, 0);
                break;
            case "FIFO":
            default:
                availableBatches = batchRepository.findByBook_IdAndRemainingQuantityGreaterThanOrderByCreatedAtAsc(bookId, 0);
                break;
        }

        if (availableBatches.isEmpty()) {
            throw new IllegalStateException("No available batches for book: " + bookId);
        }

        int totalAvailable = availableBatches.stream()
                .mapToInt(Batch::getRemainingQuantity)
                .sum();

        if (totalAvailable < quantity) {
            throw new IllegalStateException("Not enough stock. Available: " + totalAvailable + ", Requested: " + quantity);
        }

        return availableBatches.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBatchAvailable(Long batchId) throws IdInvalidException {
        validateBatchId(batchId);
        Batch batch = findBatchById(batchId);
        return batch.getRemainingQuantity() > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public int calculateTotalStockQuantityByBookId(Long bookId) throws IdInvalidException {
        validateBookId(bookId);
        validateBookExists(bookId);
        List<Batch> batches = batchRepository.findByBook_IdOrderByCreatedAtDesc(bookId);
        return batches.stream()
                .mapToInt(Batch::getRemainingQuantity)
                .sum();
    }

    @Override
    @Transactional
    public void syncBookStockQuantity(Long bookId) throws IdInvalidException {
        validateBookId(bookId);
        Book book = findBookById(bookId);
        int totalStock = calculateTotalStockQuantityByBookId(bookId);
        book.setStockQuantity(totalStock);
        bookRepository.save(book);
    }

    private Batch findBatchById(Long batchId) {
        return batchRepository.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch not found with identifier: " + batchId));
    }

    private Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));
    }

    private Variant findVariantById(Long variantId) throws IdInvalidException {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new IdInvalidException("Variant not found with id: " + variantId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + userId));
    }

    private Supplier findSupplierById(Long supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found with identifier: " + supplierId));
    }

    private ImportStockDetail findImportStockDetailById(Long importStockDetailId) {
        return importStockDetailRepository.findById(importStockDetailId)
                .orElseThrow(() -> new RuntimeException("ImportStockDetail not found with identifier: " + importStockDetailId));
    }

    private OrderDetail findOrderDetailById(Long orderDetailId) {
        return orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found with identifier: " + orderDetailId));
    }

    private List<BatchResponse> enrichWithSellingPrice(List<BatchResponse> responses, List<Batch> batches) {
        for (int i = 0; i < responses.size(); i++) {
            Batch batch = batches.get(i);
            BatchResponse response = responses.get(i);
            double sellingPrice = getSellingPrice(batch);
            response.setSellingPrice(sellingPrice);
        }
        return responses;
    }

    private BatchResponse enrichSingleWithSellingPrice(BatchResponse response, Batch batch) {
        double sellingPrice = getSellingPrice(batch);
        response.setSellingPrice(sellingPrice);
        return response;
    }

    private double getSellingPrice(Batch batch) {
        Book book = batch.getBook();
        Variant variant = batch.getVariant();
        return bookVariantRepository
                .findByBookIdAndVariantId(book.getId(), variant.getId())
                .map(BookVariant::getPrice)
                .orElse(book.getPrice());
    }
}
