package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBatchDetailRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBatchRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BatchDetailResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BatchResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.BatchMapper;
import vn.edu.iuh.fit.bookstorebackend.model.Batch;
import vn.edu.iuh.fit.bookstorebackend.model.BatchDetail;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.BookVariant;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStockDetail;
import vn.edu.iuh.fit.bookstorebackend.model.OrderDetail;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;
import vn.edu.iuh.fit.bookstorebackend.repository.BatchDetailRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.BatchRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.BookVariantRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.ImportStockDetailRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.OrderDetailRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.BatchService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final BatchDetailRepository batchDetailRepository;
    private final BookRepository bookRepository;
    private final BookVariantRepository bookVariantRepository;
    private final UserRepository userRepository;
    private final ImportStockDetailRepository importStockDetailRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final BatchMapper batchMapper;

    @Override
    @Transactional
    public BatchResponse createBatch(CreateBatchRequest request) throws IdInvalidException {
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Tạo Batch khi nhập hàng
        validateCreateBatchRequest(request);

        Book book = findBookById(request.getBookId());
        User createdBy = findUserById(request.getCreatedById());
        validateImporterRole(createdBy);

        ImportStockDetail importStockDetail = null;
        if (request.getImportStockDetailId() != null) {
            importStockDetail = findImportStockDetailById(request.getImportStockDetailId());
        }

        String batchCode = request.getBatchCode() != null && !request.getBatchCode().isEmpty()
                ? request.getBatchCode()
                : generateBatchCode();

        Batch batch = createBatchFromRequest(request, book, createdBy, importStockDetail, batchCode);
        Batch savedBatch = batchRepository.save(batch);

        syncBookStockQuantity(book.getId());

        return batchMapper.toBatchResponse(savedBatch);
    }

    private void validateCreateBatchRequest(CreateBatchRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateBatchRequest cannot be null");
        }
        if (request.getBookId() == null || request.getBookId() <= 0) {
            throw new IdInvalidException("Book identifier is invalid");
        }
        if (request.getCreatedById() == null || request.getCreatedById() <= 0) {
            throw new IdInvalidException("User identifier is invalid");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IdInvalidException("Quantity must be greater than 0");
        }
        if (request.getImportPrice() == null || request.getImportPrice() < 0) {
            throw new IdInvalidException("Import price cannot be negative");
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
            throw new RuntimeException("User does not have permission to create batch. Required roles: ADMIN or WAREHOUSE_STAFF");
        }
    }

    private String generateBatchCode() {
        String prefix = "LH";
        String month = String.format("%02d", LocalDateTime.now().getMonthValue());
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = batchRepository.count() + 1;
        return String.format("%s-%s-%s-%04d", prefix, month, year, count);
    }

    private Batch createBatchFromRequest(CreateBatchRequest request, Book book, User createdBy, ImportStockDetail importStockDetail, String batchCode) {
        Batch batch = new Batch();
        batch.setBatchCode(batchCode);
        batch.setQuantity(request.getQuantity());
        batch.setRemainingQuantity(request.getQuantity());
        batch.setImportPrice(request.getImportPrice());
        batch.setProductionDate(request.getProductionDate());
        batch.setManufacturer(request.getManufacturer());
        batch.setCreatedAt(LocalDateTime.now());
        batch.setBook(book);
        batch.setCreatedBy(createdBy);
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
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Lấy tất cả Batch
        User currentUser = getCurrentUser();
        validateImporterRole(currentUser);
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
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Lấy Batch theo User
        User currentUser = getCurrentUser();
        validateImporterRole(currentUser);
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
        validateCreateBatchDetailRequest(request);

        Batch batch = findBatchById(request.getBatchId());
        OrderDetail orderDetail = findOrderDetailById(request.getOrderDetailId());

        validateBatchAvailability(batch, request.getQuantity());

        BatchDetail batchDetail = createBatchDetailFromRequest(request, batch, orderDetail);
        BatchDetail savedBatchDetail = batchDetailRepository.save(batchDetail);

        updateBatchRemainingQuantity(batch.getId(), request.getQuantity());

        return batchMapper.toBatchDetailResponse(savedBatchDetail);
    }

    private void validateCreateBatchDetailRequest(CreateBatchDetailRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateBatchDetailRequest cannot be null");
        }
        if (request.getBatchId() == null || request.getBatchId() <= 0) {
            throw new IdInvalidException("Batch identifier is invalid");
        }
        if (request.getOrderDetailId() == null || request.getOrderDetailId() <= 0) {
            throw new IdInvalidException("OrderDetail identifier is invalid");
        }
        if (request.getQuantity() <= 0) {
            throw new IdInvalidException("Quantity must be greater than 0");
        }
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
        // Role: ADMIN hoặc WAREHOUSE_STAFF - Đồng bộ Book.stockQuantity với tổng Batch.remainingQuantity
        User currentUser = getCurrentUser();
        validateImporterRole(currentUser);
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

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + userId));
    }

    private ImportStockDetail findImportStockDetailById(Long importStockDetailId) {
        return importStockDetailRepository.findById(importStockDetailId)
                .orElseThrow(() -> new RuntimeException("ImportStockDetail not found with identifier: " + importStockDetailId));
    }

    private OrderDetail findOrderDetailById(Long orderDetailId) {
        return orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new RuntimeException("OrderDetail not found with identifier: " + orderDetailId));
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        validateAuthentication(auth);

        String email = extractEmailFromAuth(auth);
        User user = findUserByEmail(email);
        validateUserIsActive(user);

        return user;
    }

    private void validateAuthentication(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("User is not authenticated. Please login first.");
        }
    }

    private String extractEmailFromAuth(Authentication auth) {
        String email = auth.getName();
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email not found in authentication token");
        }
        return email;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    private void validateUserIsActive(User user) {
        if (user == null) {
            throw new RuntimeException("User is null");
        }
        // Có thể thêm check isActive nếu User có field này
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
