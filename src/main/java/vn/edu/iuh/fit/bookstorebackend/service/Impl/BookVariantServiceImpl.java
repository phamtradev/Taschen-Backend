package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBookVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateBookVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BookVariantResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.mapper.BookVariantMapper;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.BookVariant;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.BookVariantRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.VariantRepository;
import vn.edu.iuh.fit.bookstorebackend.service.BookVariantService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookVariantServiceImpl implements BookVariantService {

    private final BookVariantRepository bookVariantRepository;
    private final BookRepository bookRepository;
    private final VariantRepository variantRepository;
    private final BookVariantMapper bookVariantMapper;

    @Override
    @Transactional
    public BookVariantResponse createBookVariant(CreateBookVariantRequest request) throws IdInvalidException {
        validateCreateRequest(request);

        Book book = findBookById(request.getBookId());
        Variant variant = findVariantById(request.getVariantId());
        validateBookVariantNotExists(book.getId(), variant.getId());

        BookVariant bookVariant = createBookVariantFromRequest(request, book, variant);
        BookVariant saved = bookVariantRepository.save(bookVariant);

        return bookVariantMapper.toBookVariantResponse(saved);
    }

    private void validateCreateRequest(CreateBookVariantRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("CreateBookVariantRequest cannot be null");
        }
        if (request.getBookId() == null || request.getBookId() <= 0) {
            throw new IdInvalidException("Book id is invalid: " + request.getBookId());
        }
        if (request.getVariantId() == null || request.getVariantId() <= 0) {
            throw new IdInvalidException("Variant id is invalid: " + request.getVariantId());
        }
    }

    private Book findBookById(Long bookId) throws IdInvalidException {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new IdInvalidException("Book not found with id: " + bookId));
    }

    private Variant findVariantById(Long variantId) throws IdInvalidException {
        return variantRepository.findById(variantId)
                .orElseThrow(() -> new IdInvalidException("Variant not found with id: " + variantId));
    }

    private void validateBookVariantNotExists(Long bookId, Long variantId) {
        if (bookVariantRepository.existsByBookIdAndVariantId(bookId, variantId)) {
            throw new RuntimeException("BookVariant already exists for book: " + bookId + " and variant: " + variantId);
        }
    }

    private BookVariant createBookVariantFromRequest(CreateBookVariantRequest request, Book book, Variant variant) {
        BookVariant bookVariant = new BookVariant();
        bookVariant.setBook(book);
        bookVariant.setVariant(variant);
        bookVariant.setPrice(request.getPrice() != null ? request.getPrice() : 0.0);
        bookVariant.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        return bookVariant;
    }

    @Override
    @Transactional(readOnly = true)
    public BookVariantResponse getBookVariantById(Long id) throws IdInvalidException {
        validateBookVariantId(id);
        BookVariant bookVariant = findBookVariantById(id);
        return bookVariantMapper.toBookVariantResponse(bookVariant);
    }

    private void validateBookVariantId(Long id) throws IdInvalidException {
        if (id == null || id <= 0) {
            throw new IdInvalidException("BookVariant id is invalid: " + id);
        }
    }

    private BookVariant findBookVariantById(Long id) throws IdInvalidException {
        return bookVariantRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("BookVariant not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookVariantResponse> getBookVariantsByBookId(Long bookId) throws IdInvalidException {
        validateBookId(bookId);
        List<BookVariant> bookVariants = bookVariantRepository.findByBookId(bookId);
        return mapToBookVariantResponseList(bookVariants);
    }

    private void validateBookId(Long bookId) throws IdInvalidException {
        if (bookId == null || bookId <= 0) {
            throw new IdInvalidException("Book id is invalid: " + bookId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookVariantResponse> getBookVariantsByVariantId(Long variantId) throws IdInvalidException {
        validateVariantId(variantId);
        List<BookVariant> bookVariants = bookVariantRepository.findByVariantId(variantId);
        return mapToBookVariantResponseList(bookVariants);
    }

    private void validateVariantId(Long variantId) throws IdInvalidException {
        if (variantId == null || variantId <= 0) {
            throw new IdInvalidException("Variant id is invalid: " + variantId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookVariantResponse> getAllBookVariants() {
        List<BookVariant> bookVariants = bookVariantRepository.findAll();
        return mapToBookVariantResponseList(bookVariants);
    }

    private List<BookVariantResponse> mapToBookVariantResponseList(List<BookVariant> bookVariants) {
        return bookVariants.stream()
                .map(bookVariantMapper::toBookVariantResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookVariantResponse updateBookVariant(Long id, UpdateBookVariantRequest request) throws IdInvalidException {
        validateBookVariantId(id);
        validateUpdateRequest(request);

        BookVariant bookVariant = findBookVariantById(id);
        updateBookVariantFields(bookVariant, request);

        BookVariant updated = bookVariantRepository.save(bookVariant);
        return bookVariantMapper.toBookVariantResponse(updated);
    }

    private void validateUpdateRequest(UpdateBookVariantRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("UpdateBookVariantRequest cannot be null");
        }
    }

    private void updateBookVariantFields(BookVariant bookVariant, UpdateBookVariantRequest request) {
        if (request.getPrice() != null) {
            bookVariant.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            bookVariant.setStockQuantity(request.getStockQuantity());
        }
    }

    @Override
    @Transactional
    public void deleteBookVariant(Long id) throws IdInvalidException {
        validateBookVariantId(id);
        BookVariant bookVariant = findBookVariantById(id);
        bookVariantRepository.delete(bookVariant);
    }
}
