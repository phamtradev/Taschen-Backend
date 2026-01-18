package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateBookRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BookResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface BookService {
    
    BookResponse createBook(CreateBookRequest request) throws IdInvalidException;
    
    BookResponse getBookById(Long bookId) throws IdInvalidException;
    
    List<BookResponse> getAllBooks();
    
    BookResponse updateBook(Long bookId, UpdateBookRequest request) throws IdInvalidException;
    
    void deleteBook(Long bookId) throws IdInvalidException;
    
    List<BookResponse> getAllBooksSorted(String sortByField, String sortDirection);
}
