package vn.edu.iuh.fit.bookstorebackend.book.service;

import org.springframework.data.domain.Pageable;
import vn.edu.iuh.fit.bookstorebackend.shared.dto.response.PageResponse;
import vn.edu.iuh.fit.bookstorebackend.book.dto.request.CreateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.book.dto.request.UpdateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.book.dto.response.BookResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface BookService {
    
    BookResponse createBook(CreateBookRequest request) throws IdInvalidException;
    
    BookResponse getBookById(Long bookId) throws IdInvalidException;
    
    List<BookResponse> getAllBooks();

    PageResponse<BookResponse> getAllBooks(Pageable pageable);
    
    BookResponse updateBook(Long bookId, UpdateBookRequest request) throws IdInvalidException;
    
    void deleteBook(Long bookId) throws IdInvalidException;
    
    List<BookResponse> getAllBooksSorted(String sortByField, String sortDirection);
    
    List<BookResponse> getBooksByCategoryId(Long categoryId) throws IdInvalidException;
    
    List<BookResponse> getBooksBySupplierId(Long supplierId) throws IdInvalidException;

    List<BookResponse> searchBooks(String keyword, Long categoryId, String sortBy, String status);

    PageResponse<BookResponse> searchBooks(String keyword, Long categoryId, String status, Pageable pageable);

    BookResponse restoreBook(Long bookId) throws IdInvalidException;
}
