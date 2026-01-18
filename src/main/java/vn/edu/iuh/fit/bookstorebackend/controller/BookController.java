package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BookResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@RequestBody CreateBookRequest request) throws IdInvalidException {
        BookResponse bookResponse = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookResponse);
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> getAllBooks(
            @RequestParam(required = false) String sortByField,
            @RequestParam(required = false) String sortDirection) {
        List<BookResponse> books;
        if (sortByField != null || sortDirection != null) {
            books = bookService.getAllBooksSorted(
                    sortByField != null ? sortByField : "id",
                    sortDirection != null ? sortDirection : "asc"
            );
        } else {
            books = bookService.getAllBooks();
        }
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    @GetMapping("/sorted")
    public ResponseEntity<List<BookResponse>> getAllBooksSorted(
            @RequestParam(required = false, defaultValue = "id") String sortByField,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection) {
        List<BookResponse> books = bookService.getAllBooksSorted(sortByField, sortDirection);
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<BookResponse>> getBooksByCategoryId(@PathVariable Long categoryId) throws IdInvalidException {
        List<BookResponse> books = bookService.getBooksByCategoryId(categoryId);
        return ResponseEntity.status(HttpStatus.OK).body(books);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBookById(@PathVariable Long bookId) throws IdInvalidException {
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
    public ResponseEntity<Void> deleteBook(@PathVariable Long bookId) throws IdInvalidException {
        bookService.deleteBook(bookId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
