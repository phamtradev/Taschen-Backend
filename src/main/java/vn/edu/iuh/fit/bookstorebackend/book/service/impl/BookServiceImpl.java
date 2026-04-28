package vn.edu.iuh.fit.bookstorebackend.book.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.book.dto.request.CreateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.book.dto.request.UpdateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.book.dto.response.BookResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.dto.response.PageResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.book.model.Book;
import vn.edu.iuh.fit.bookstorebackend.book.model.BookVariant;
import vn.edu.iuh.fit.bookstorebackend.book.model.Category;
import vn.edu.iuh.fit.bookstorebackend.supplier.model.Supplier;
import vn.edu.iuh.fit.bookstorebackend.book.model.Variant;
import vn.edu.iuh.fit.bookstorebackend.book.mapper.BookMapper;
import vn.edu.iuh.fit.bookstorebackend.book.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.book.repository.BookVariantRepository;
import vn.edu.iuh.fit.bookstorebackend.book.repository.CategoryRepository;
import vn.edu.iuh.fit.bookstorebackend.supplier.repository.SupplierRepository;
import vn.edu.iuh.fit.bookstorebackend.book.repository.VariantRepository;
import vn.edu.iuh.fit.bookstorebackend.book.service.BookEmbeddingService;
import vn.edu.iuh.fit.bookstorebackend.book.service.BookService;

import vn.edu.iuh.fit.bookstorebackend.shared.util.PaginationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookVariantRepository bookVariantRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final VariantRepository variantRepository;
    private final BookMapper bookMapper;
    private final BookEmbeddingService bookEmbeddingService;

    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) throws IdInvalidException {
        validateRequest(request);
        validateTitleWithVariants(request.getTitle(), request.getVariantIds());
        
        Supplier supplier = findSupplierById(request.getSupplierId());
        
        Book book = createBookFromRequest(request, supplier);
        setBookCategories(book, request.getCategoryIds());
        
        Book savedBook = bookRepository.save(book);

        setBookVariants(savedBook, request.getVariantIds());
        bookVariantRepository.saveAll(savedBook.getBookVariants());

        // NOTE: Embedding generation runs asynchronously - does not block response
        bookEmbeddingService.generateEmbedding(savedBook.getId());

        return bookMapper.toBookResponse(savedBook);
    }
    
    private Supplier findSupplierById(Long supplierId) throws IdInvalidException {
        if (supplierId == null) {
            return null;
        }
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IdInvalidException("Supplier not found with identifier: " + supplierId));
    }
    
    private void validateRequest(CreateBookRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateBookRequest cannot be null");
        }
    }
    
    private void validateTitleWithVariants(String title, List<Long> variantIds) throws IdInvalidException {
        if (title != null && variantIds != null && !variantIds.isEmpty()) {
            if (bookRepository.existsByTitleAndVariantIds(title, variantIds)) {
                throw new IdInvalidException("Book with title '" + title + "' and this variant already exists");
            }
        }
    } 
    
    private Book createBookFromRequest(CreateBookRequest request, Supplier supplier) {
        Book book = bookMapper.toBook(request);
        book.setPrice(request.getPrice() != null ? request.getPrice() : 0.0);
        book.setStockQuantity(0);
        book.setIsActive(request.getIsActive() != null 
                ? request.getIsActive() : true);
        book.setSupplier(supplier);
        return book;
    }
    
    private void setBookCategories(Book book, List<Long> categoryIds) throws IdInvalidException {
        if (categoryIds == null || categoryIds.isEmpty()) {
            book.setCategories(new ArrayList<>());
            return;
        }
        
        Set<Category> categoriesSet = categoryRepository.findByIdIn(categoryIds);
        if (categoriesSet.size() != categoryIds.size()) {
            throw new IdInvalidException("One or more category identifiers are invalid");
        }
        book.setCategories(new ArrayList<>(categoriesSet));
    }
    
    private void setBookVariants(Book book, List<Long> variantIds) throws IdInvalidException {
        if (variantIds == null || variantIds.isEmpty()) {
            if (book.getBookVariants() != null) {
                book.getBookVariants().clear();
            }
            return;
        }

        // Clear existing variants before adding new ones (required for orphanRemoval = true)
        if (book.getBookVariants() != null) {
            book.getBookVariants().clear();
        } else {
            book.setBookVariants(new ArrayList<>());
        }

        List<BookVariant> bookVariants = book.getBookVariants();
        for (Long variantId : variantIds) {
            Variant variant = variantRepository.findById(variantId)
                    .orElseThrow(() -> new IdInvalidException("Variant not found with id: " + variantId));

            BookVariant bookVariant = new BookVariant();
            bookVariant.setBook(book);
            bookVariant.setVariant(variant);
            bookVariant.setPrice(book.getPrice());
            bookVariant.setStockQuantity(book.getStockQuantity());
            bookVariants.add(bookVariant);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long bookId) throws IdInvalidException {
        validateBookId(bookId);
        Book book = findBookById(bookId);
        return bookMapper.toBookResponse(book);
    }

    @Override
    public List<BookResponse> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return mapToBookResponseList(books);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> getAllBooks(Pageable pageable) {
        Page<Book> page = bookRepository.findAll(pageable);
        Page<BookResponse> mappedPage = page.map(bookMapper::toBookResponse);
        return PaginationUtil.toPageResponse(mappedPage);
    }
    
    private void validateBookId(Long bookId) throws IdInvalidException {
        if (bookId == null || bookId <= 0) {
            throw new IdInvalidException("Book identifier is invalid: " + bookId);
        }
    }
    
    private Book findBookById(Long bookId) {
        return bookRepository.findByIdAndIsActiveNotNull(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long bookId, UpdateBookRequest request) throws IdInvalidException {
        validateBookId(bookId);
        validateRequest(request);
        
        Book book = findBookById(bookId);
        updateBookFields(book, request);
        updateBookCategories(book, request.getCategoryIds());
        
        Book updatedBook = bookRepository.save(book);
        List<Long> effectiveVariantIds = request.getVariantIds() != null
                ? request.getVariantIds()
                : (request.getFormatId() != null ? List.of(request.getFormatId()) : null);
        updateBookVariants(updatedBook, effectiveVariantIds);

        // Fire-and-forget: regenerate embedding asynchronously
        bookEmbeddingService.regenerateEmbedding(bookId);

        return bookMapper.toBookResponse(updatedBook);
    }
    
    private void validateRequest(UpdateBookRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("UpdateBookRequest cannot be null");
        }
    }
    
    private void updateBookFields(Book book, UpdateBookRequest request) throws IdInvalidException {
        if (request.getTitle() != null && !request.getTitle().equals(book.getTitle())) {
            validateTitleWithVariants(request.getTitle(), request.getVariantIds());
            book.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            book.setAuthor(request.getAuthor());
        }
        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }
        if (request.getPublicationYear() != null) {
            book.setPublicationYear(request.getPublicationYear());
        }
        if (request.getWeightGrams() != null) {
            book.setWeightGrams(request.getWeightGrams());
        }
        if (request.getPageCount() != null) {
            book.setPageCount(request.getPageCount());
        }
        if (request.getPrice() != null) {
            book.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            book.setStockQuantity(request.getStockQuantity());
        }
        if (request.getImageUrl() != null) {
            book.setImageUrl(request.getImageUrl());
        }
        if (request.getIsActive() != null) {
            book.setIsActive(request.getIsActive());
        }
        if (request.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(request.getSupplierId())
                    .orElseThrow(() -> new IdInvalidException("Supplier not found"));
            book.setSupplier(supplier);
        }
    }
    
    private void updateBookCategories(Book book, List<Long> categoryIds) throws IdInvalidException {
        if (categoryIds == null) {
            return;
        }
        
        if (categoryIds.isEmpty()) {
            book.setCategories(new ArrayList<>());
        } else {
            Set<Category> categoriesSet = categoryRepository.findByIdIn(categoryIds);
            if (categoriesSet.size() != categoryIds.size()) {
                throw new IdInvalidException("One or more category identifiers are invalid");
            }
            book.setCategories(new ArrayList<>(categoriesSet));
        }
    }
    
    private void updateBookVariants(Book book, List<Long> variantIds) throws IdInvalidException {
        setBookVariants(book, variantIds);
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) throws IdInvalidException {
        validateBookId(bookId);
        Book book = findBookById(bookId);

        book.setIsActive(null);
        bookRepository.save(book);

        bookEmbeddingService.deleteEmbedding(bookId);
    }

    @Override
    public List<BookResponse> getAllBooksSorted(String sortByField, String sortDirection) {
        String field = getSortField(sortByField);
        Sort.Direction direction = getSortDirection(sortDirection);

        Sort sort = Sort.by(direction, field);
        List<Book> books = bookRepository.findAll(sort);

        return mapToBookResponseList(books);
    }
    
    private String getSortField(String sortByField) {
        if (sortByField == null || sortByField.trim().isEmpty()) {
            return "id";
        }
        return sortByField;
    }
    
    private Sort.Direction getSortDirection(String sortDirection) {
        if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByCategoryId(Long categoryId) throws IdInvalidException {
        validateCategoryId(categoryId);
        validateCategoryExists(categoryId);

        List<Book> books = bookRepository.findByCategoryIdWithCategories(categoryId);
        return mapToBookResponseList(books);
    }
    
    private void validateCategoryId(Long categoryId) throws IdInvalidException {
        if (categoryId == null || categoryId <= 0) {
            throw new IdInvalidException("Category identifier is invalid: " + categoryId);
        }
    }
    
    private void validateCategoryExists(Long categoryId) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException(
                        "Category not found with identifier: " + categoryId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksBySupplierId(Long supplierId) throws IdInvalidException {
        if (supplierId == null || supplierId <= 0) {
            throw new IdInvalidException("Supplier identifier is invalid: " + supplierId);
        }

        List<Book> books = bookRepository.findBySupplierId(supplierId);
        return mapToBookResponseList(books);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> searchBooks(String keyword, Long categoryId, String sortBy, String status) {
        List<Book> books = bookRepository.searchBooks(keyword, categoryId, sortBy, resolveStatus(status));
        return mapToBookResponseList(books);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BookResponse> searchBooks(String keyword, Long categoryId, String status, Pageable pageable) {
        Page<Book> page = bookRepository.searchBooks(keyword, categoryId, resolveStatus(status), pageable);
        Page<BookResponse> mappedPage = page.map(bookMapper::toBookResponse);
        return PaginationUtil.toPageResponse(mappedPage);
    }

    @Override
    @Transactional
    public BookResponse restoreBook(Long bookId) throws IdInvalidException {
        validateBookId(bookId);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IdInvalidException("Book not found with identifier: " + bookId));

        if (book.getIsActive() != null) {
            throw new IdInvalidException("Book is not soft-deleted and cannot be restored");
        }

        book.setIsActive(true);
        return bookMapper.toBookResponse(bookRepository.save(book));
    }

    private String resolveStatus(String status) {
        if (status == null) return "active";
        return switch (status.toLowerCase()) {
            case "deleted", "all" -> status.toLowerCase();
            default -> "active";
        };
    }

    private List<BookResponse> mapToBookResponseList(List<Book> books) {
        return books.stream()
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
    }
}
