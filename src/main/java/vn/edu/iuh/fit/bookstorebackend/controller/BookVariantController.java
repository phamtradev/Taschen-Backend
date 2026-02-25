package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBookVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateBookVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BookVariantResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.BookVariantService;

import java.util.List;

@RestController
@RequestMapping("/api/book-variants")
public class BookVariantController {

    private final BookVariantService bookVariantService;

    public BookVariantController(BookVariantService bookVariantService) {
        this.bookVariantService = bookVariantService;
    }

    @PostMapping
    public ResponseEntity<BookVariantResponse> createBookVariant(
            @RequestBody CreateBookVariantRequest request) throws IdInvalidException {
        BookVariantResponse response = bookVariantService.createBookVariant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BookVariantResponse>> getAllBookVariants() {
        List<BookVariantResponse> responses = bookVariantService.getAllBookVariants();
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookVariantResponse> getBookVariantById(
            @PathVariable Long id) throws IdInvalidException {
        BookVariantResponse response = bookVariantService.getBookVariantById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<BookVariantResponse>> getBookVariantsByBookId(
            @PathVariable Long bookId) throws IdInvalidException {
        List<BookVariantResponse> responses = bookVariantService.getBookVariantsByBookId(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/variant/{variantId}")
    public ResponseEntity<List<BookVariantResponse>> getBookVariantsByVariantId(
            @PathVariable Long variantId) throws IdInvalidException {
        List<BookVariantResponse> responses = bookVariantService.getBookVariantsByVariantId(variantId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookVariantResponse> updateBookVariant(
            @PathVariable Long id,
            @RequestBody UpdateBookVariantRequest request) throws IdInvalidException {
        BookVariantResponse response = bookVariantService.updateBookVariant(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBookVariant(
            @PathVariable Long id) throws IdInvalidException {
        bookVariantService.deleteBookVariant(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
