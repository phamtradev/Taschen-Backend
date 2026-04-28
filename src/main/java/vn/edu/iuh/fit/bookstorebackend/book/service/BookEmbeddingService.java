package vn.edu.iuh.fit.bookstorebackend.book.service;

import vn.edu.iuh.fit.bookstorebackend.book.dto.response.BookResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

public interface BookEmbeddingService {

    void generateEmbedding(Long bookId) throws IdInvalidException;

    void regenerateEmbedding(Long bookId) throws IdInvalidException;

    List<BookResponse> findSimilarBooks(Long bookId, int limit) throws IdInvalidException;

    List<BookResponse> findSimilarByText(String queryText, int limit);

    void deleteEmbedding(Long bookId);
}
