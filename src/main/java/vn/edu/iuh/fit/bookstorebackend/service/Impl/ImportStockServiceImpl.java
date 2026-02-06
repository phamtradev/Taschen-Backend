package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateImportStockRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.ImportStockResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.ImportStockMapper;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStock;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStockDetail;
import vn.edu.iuh.fit.bookstorebackend.model.Supplier;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.ImportStockDetailRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.ImportStockRepository;
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
    private final ImportStockMapper importStockMapper;

    @Override
    @Transactional
    public ImportStockResponse createImportStock(CreateImportStockRequest request) throws IdInvalidException {
        validateCreateImportStockRequest(request);

        Supplier supplier = findSupplierById(request.getSupplierId());
        User createdBy = findUserById(request.getCreatedById());

        ImportStock importStock = createImportStockFromRequest(request, supplier, createdBy);
        ImportStock savedImportStock = importStockRepository.save(importStock);

        createImportStockDetails(savedImportStock, request.getDetails());

        return importStockMapper.toImportStockResponse(savedImportStock);
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

    private ImportStock createImportStockFromRequest(CreateImportStockRequest request, Supplier supplier, User createdBy) {
        ImportStock importStock = new ImportStock();
        importStock.setImportDate(LocalDateTime.now());
        importStock.setSupplier(supplier);
        importStock.setCreatedBy(createdBy);
        return importStock;
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
        return importStocks.stream()
                .map(importStockMapper::toImportStockResponse)
                .collect(Collectors.toList());
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
}
