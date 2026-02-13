package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.response.BookResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

public interface BookEmbeddingService {

    void generateEmbedding(Long bookId) throws IdInvalidException;

    void regenerateEmbedding(Long bookId) throws IdInvalidException;

    List<BookResponse> findSimilarBooks(Long bookId, int limit) throws IdInvalidException;

    void deleteEmbedding(Long bookId);
}
