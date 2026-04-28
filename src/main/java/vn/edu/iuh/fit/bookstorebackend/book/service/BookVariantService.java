package vn.edu.iuh.fit.bookstorebackend.book.service;

import vn.edu.iuh.fit.bookstorebackend.book.dto.request.CreateBookVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.book.dto.request.UpdateBookVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.book.dto.response.BookVariantResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface BookVariantService {

    BookVariantResponse createBookVariant(CreateBookVariantRequest request) throws IdInvalidException;

    BookVariantResponse getBookVariantById(Long id) throws IdInvalidException;

    List<BookVariantResponse> getBookVariantsByBookId(Long bookId) throws IdInvalidException;

    List<BookVariantResponse> getBookVariantsByVariantId(Long variantId) throws IdInvalidException;

    List<BookVariantResponse> getAllBookVariants();

    BookVariantResponse updateBookVariant(Long id, UpdateBookVariantRequest request) throws IdInvalidException;

    void deleteBookVariant(Long id) throws IdInvalidException;
}
