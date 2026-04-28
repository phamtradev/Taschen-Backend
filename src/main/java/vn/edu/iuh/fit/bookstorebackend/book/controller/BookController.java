package vn.edu.iuh.fit.bookstorebackend.book.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.bookstorebackend.book.dto.request.CreateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.book.dto.request.UpdateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.book.dto.response.BookResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.dto.response.PageResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.book.service.BookEmbeddingService;
import vn.edu.iuh.fit.bookstorebackend.book.service.BookService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@Validated
public class BookController {

    private final BookService bookService;
    private final BookEmbeddingService bookEmbeddingService;

    public BookController(BookService bookService, BookEmbeddingService bookEmbeddingService) {
        this.bookService = bookService;
        this.bookEmbeddingService = bookEmbeddingService;
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody CreateBookRequest request) throws IdInvalidException {
        BookResponse bookResponse = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookResponse);
    }

    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> getAllBooks(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));
        
        PageResponse<BookResponse> bookPage = bookService.getAllBooks(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(bookPage);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BookResponse>> getAllBooksWithoutPagination() {
        List<BookResponse> books = bookService.getAllBooks();
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    @GetMapping("/sorted")
    public ResponseEntity<List<BookResponse>> getAllBooksSorted(
            @RequestParam(required = false, defaultValue = "id") String sortByField,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        List<BookResponse> books = bookService.getAllBooksSorted(
                sortByField, sortDirection);
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<BookResponse>> getBooksByCategoryId(
            @PathVariable Long categoryId) throws IdInvalidException {
        List<BookResponse> books = bookService.getBooksByCategoryId(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<BookResponse>> getBooksBySupplierId(
            @PathVariable Long supplierId) throws IdInvalidException {
        List<BookResponse> books = bookService.getBooksBySupplierId(supplierId);
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBookById(
            @PathVariable Long bookId) throws IdInvalidException {
        BookResponse bookResponse = bookService.getBookById(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(bookResponse);
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long bookId,
            @RequestBody UpdateBookRequest request) throws IdInvalidException {
        BookResponse bookResponse = bookService.updateBook(bookId, request);
        return ResponseEntity.status(HttpStatus.OK).body(bookResponse);
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Map<String, String>> deleteBook(
            @PathVariable Long bookId) throws IdInvalidException {
        bookService.deleteBook(bookId);
        return ResponseEntity.ok(Map.of("message", "Book soft-deleted successfully"));
    }

    @PutMapping("/{bookId}/restore")
    public ResponseEntity<BookResponse> restoreBook(
            @PathVariable Long bookId) throws IdInvalidException {
        BookResponse bookResponse = bookService.restoreBook(bookId);
        return ResponseEntity.ok(bookResponse);
    }

    @GetMapping("/{bookId}/similar")
    public ResponseEntity<List<BookResponse>> findSimilarBooks(
            @PathVariable Long bookId,
            @RequestParam(required = false, defaultValue = "10") int limit) throws IdInvalidException {
        List<BookResponse> similarBooks = bookEmbeddingService.findSimilarBooks(bookId, limit);
        return ResponseEntity.status(HttpStatus.OK).body(similarBooks);
    }

    @GetMapping("/search/similar")
    public ResponseEntity<List<BookResponse>> findSimilarByText(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "10") int limit) {
        List<BookResponse> similarBooks = bookEmbeddingService.findSimilarByText(query, limit);
        return ResponseEntity.status(HttpStatus.OK).body(similarBooks);
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<BookResponse>> searchBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, defaultValue = "active") String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));

        PageResponse<BookResponse> result = bookService.searchBooks(keyword, categoryId, status, pageable);
        return ResponseEntity.ok(result);
    }
}
