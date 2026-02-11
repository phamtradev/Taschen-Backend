package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BookResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PageResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.Category;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;
import vn.edu.iuh.fit.bookstorebackend.mapper.BookMapper;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.CategoryRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.VariantRepository;
import vn.edu.iuh.fit.bookstorebackend.service.BookService;

import vn.edu.iuh.fit.bookstorebackend.util.PaginationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final VariantRepository variantRepository;
    private final BookMapper bookMapper;

    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) throws IdInvalidException {
        validateRequest(request);
        validateTitleNotExists(request.getTitle());
        
        Book book = createBookFromRequest(request);
        setBookCategories(book, request.getCategoryIds());
        
        Book savedBook = bookRepository.save(book);
        createBookVariants(savedBook, request.getVariantFormats());
        
        return bookMapper.toBookResponse(savedBook);
    }
    
    private void validateRequest(CreateBookRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateBookRequest cannot be null");
        }
    }
    
    private void validateTitleNotExists(String title) {
        if (title != null && bookRepository.existsByTitle(title)) {
            throw new RuntimeException("Book with title already exists: " + title);
        }
    }
    
    private Book createBookFromRequest(CreateBookRequest request) {
        Book book = bookMapper.toBook(request);
        book.setPrice(request.getPrice() != null ? request.getPrice() : 0.0);
        book.setStockQuantity(request.getStockQuantity() != null 
                ? request.getStockQuantity() : 0);
        book.setIsActive(request.getIsActive() != null 
                ? request.getIsActive() : true);
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
    
    private void createBookVariants(Book book, List<String> variantFormats) {
        if (variantFormats == null || variantFormats.isEmpty()) {
            return;
        }
        
        List<Variant> variants = variantFormats.stream()
                .filter(format -> format != null && !format.trim().isEmpty())
                .map(format -> createVariant(book, format.trim()))
                .collect(Collectors.toList());
        
        if (!variants.isEmpty()) {
            variantRepository.saveAll(variants);
        }
    }
    
    private Variant createVariant(Book book, String format) {
        Variant variant = new Variant();
        variant.setFormat(format);
        variant.setBook(book);
        return variant;
    }

    @Override
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
        return bookRepository.findById(bookId)
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
        updateBookVariants(updatedBook, request.getVariantFormats());
        
        return bookMapper.toBookResponse(updatedBook);
    }
    
    private void validateRequest(UpdateBookRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("UpdateBookRequest cannot be null");
        }
    }
    
    private void updateBookFields(Book book, UpdateBookRequest request) {
        if (request.getTitle() != null && !request.getTitle().equals(book.getTitle())) {
            validateTitleNotExists(request.getTitle());
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
    
    private void updateBookVariants(Book book, List<String> variantFormats) {
        if (variantFormats == null) {
            return;
        }
        
        deleteExistingVariants(book);
        
        if (!variantFormats.isEmpty()) {
            createBookVariants(book, variantFormats);
        }
    }
    
    private void deleteExistingVariants(Book book) {
        List<Variant> existingVariants = variantRepository.findByBook(book);
        if (existingVariants != null && !existingVariants.isEmpty()) {
            variantRepository.deleteAll(existingVariants);
        }
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) throws IdInvalidException {
        validateBookId(bookId);
        Book book = findBookById(bookId);
        bookRepository.delete(book);
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

    private List<BookResponse> mapToBookResponseList(List<Book> books) {
        return books.stream()
                .map(bookMapper::toBookResponse)
                .collect(Collectors.toList());
    }
}
