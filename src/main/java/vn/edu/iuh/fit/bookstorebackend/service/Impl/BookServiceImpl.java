package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BookResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.Category;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.CategoryRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.VariantRepository;
import vn.edu.iuh.fit.bookstorebackend.service.BookService;

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

    @Override
    @Transactional
    public BookResponse createBook(CreateBookRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateBookRequest cannot be null");
        }

        // Check if book with same title already exists
        if (request.getTitle() != null && bookRepository.existsByTitle(request.getTitle())) {
            throw new RuntimeException("Book with title already exists: " + request.getTitle());
        }

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setDescription(request.getDescription());
        book.setPublicationYear(request.getPublicationYear());
        book.setWeightGrams(request.getWeightGrams());
        book.setPageCount(request.getPageCount());
        book.setPrice(request.getPrice() != null ? request.getPrice() : 0.0);
        book.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        book.setImageUrl(request.getImageUrl());
        book.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        // Set categories if provided
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            // Pass List directly - repository method now accepts List<Long>
            Set<Category> categoriesSet = categoryRepository.findByIdIn(request.getCategoryIds());
            if (categoriesSet.size() != request.getCategoryIds().size()) {
                throw new IdInvalidException("One or more category identifiers are invalid");
            }
            // Convert Set to List
            List<Category> categoriesList = new ArrayList<>(categoriesSet);
            book.setCategories(categoriesList);
        } else {
            book.setCategories(new ArrayList<>());
        }

        Book savedBook = bookRepository.save(book);

        // Create variants if provided
        if (request.getVariantFormats() != null && !request.getVariantFormats().isEmpty()) {
            List<Variant> variants = new ArrayList<>();
            for (String format : request.getVariantFormats()) {
                if (format != null && !format.trim().isEmpty()) {
                    Variant variant = new Variant();
                    variant.setFormat(format.trim());
                    variant.setBook(savedBook);
                    variants.add(variant);
                }
            }
            if (!variants.isEmpty()) {
                variantRepository.saveAll(variants);
            }
        }

        return convertToBookResponse(savedBook);
    }

    @Override
    public BookResponse getBookById(Long bookId) throws IdInvalidException {
        if (bookId == null || bookId <= 0) {
            throw new IdInvalidException("Book identifier is invalid: " + bookId);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));
        return convertToBookResponse(book);
    }

    @Override
    public List<BookResponse> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookResponse updateBook(Long bookId, UpdateBookRequest request) throws IdInvalidException {
        if (bookId == null || bookId <= 0) {
            throw new IdInvalidException("Book identifier is invalid: " + bookId);
        }

        if (request == null) {
            throw new IdInvalidException("UpdateBookRequest cannot be null");
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));

        // Check if title is being changed and if new title already exists for another book
        if (request.getTitle() != null && !request.getTitle().equals(book.getTitle())) {
            if (bookRepository.existsByTitle(request.getTitle())) {
                throw new RuntimeException("Book with title already exists: " + request.getTitle());
            }
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

        // Update categories if provided
        if (request.getCategoryIds() != null) {
            if (request.getCategoryIds().isEmpty()) {
                // If empty list is provided, clear categories
                book.setCategories(new ArrayList<>());
            } else {
                // Pass List directly - repository method now accepts List<Long>
                Set<Category> categoriesSet = categoryRepository.findByIdIn(request.getCategoryIds());
                if (categoriesSet.size() != request.getCategoryIds().size()) {
                    throw new IdInvalidException("One or more category identifiers are invalid");
                }
                // Convert Set to List
                List<Category> categoriesList = new ArrayList<>(categoriesSet);
                book.setCategories(categoriesList);
            }
        }
        // If categoryIds is null, keep existing categories

        Book updatedBook = bookRepository.save(book);

        // Update variants if provided
        if (request.getVariantFormats() != null) {
            // Delete existing variants
            List<Variant> existingVariants = variantRepository.findByBook(updatedBook);
            if (existingVariants != null && !existingVariants.isEmpty()) {
                variantRepository.deleteAll(existingVariants);
            }

            // Create new variants
            if (!request.getVariantFormats().isEmpty()) {
                List<Variant> variants = new ArrayList<>();
                for (String format : request.getVariantFormats()) {
                    if (format != null && !format.trim().isEmpty()) {
                        Variant variant = new Variant();
                        variant.setFormat(format.trim());
                        variant.setBook(updatedBook);
                        variants.add(variant);
                    }
                }
                if (!variants.isEmpty()) {
                    variantRepository.saveAll(variants);
                }
            }
        }

        return convertToBookResponse(updatedBook);
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) throws IdInvalidException {
        if (bookId == null || bookId <= 0) {
            throw new IdInvalidException("Book identifier is invalid: " + bookId);
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));

        bookRepository.delete(book);
    }

    @Override
    public List<BookResponse> getAllBooksSorted(String sortByField, String sortDirection) {
        if (sortByField == null || sortByField.trim().isEmpty()) {
            sortByField = "id";
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }

        Sort sort = Sort.by(direction, sortByField);
        List<Book> books = bookRepository.findAll(sort);
        return books.stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByCategoryId(Long categoryId) throws IdInvalidException {
        if (categoryId == null || categoryId <= 0) {
            throw new IdInvalidException("Category identifier is invalid: " + categoryId);
        }

        // Verify category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with identifier: " + categoryId));

        // Use query with JOIN FETCH to load categories together
        List<Book> books = bookRepository.findByCategoryIdWithCategories(categoryId);
        
        return books.stream()
                .map(this::convertToBookResponse)
                .collect(Collectors.toList());
    }

    private BookResponse convertToBookResponse(Book book) {
        BookResponse bookResponse = new BookResponse();
        bookResponse.setId(book.getId());
        bookResponse.setTitle(book.getTitle());
        bookResponse.setAuthor(book.getAuthor());
        bookResponse.setDescription(book.getDescription());
        bookResponse.setPublicationYear(book.getPublicationYear());
        bookResponse.setWeightGrams(book.getWeightGrams());
        bookResponse.setPageCount(book.getPageCount());
        bookResponse.setPrice(book.getPrice());
        bookResponse.setStockQuantity(book.getStockQuantity());
        bookResponse.setImageUrl(book.getImageUrl());
        bookResponse.setIsActive(book.getIsActive());

        // Convert variants to list of formats
        List<Variant> variants = variantRepository.findByBook(book);
        if (variants != null && !variants.isEmpty()) {
            List<String> variantFormatList = variants.stream()
                    .map(Variant::getFormat)
                    .collect(Collectors.toList());
            bookResponse.setVariantFormats(variantFormatList);
        } else {
            bookResponse.setVariantFormats(new ArrayList<>());
        }

        // Convert categories to list of identifiers
        try {
            List<Category> categories = book.getCategories();
            if (categories != null && !categories.isEmpty()) {
                List<Long> categoryIdentifierList = categories.stream()
                        .map(Category::getId)
                        .collect(Collectors.toList());
                bookResponse.setCategoryIds(categoryIdentifierList);
            } else {
                bookResponse.setCategoryIds(new ArrayList<>());
            }
        } catch (Exception e) {
            // If lazy loading fails or any other error, set empty list
            bookResponse.setCategoryIds(new ArrayList<>());
        }

        return bookResponse;
    }
}
