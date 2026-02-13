package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.BookEmbedding;

import java.util.Optional;

@Repository
public interface BookEmbeddingRepository extends JpaRepository<BookEmbedding, Long> {
    Optional<BookEmbedding> findByBookId(Long bookId);
    void deleteByBookId(Long bookId);
    boolean existsByBookId(Long bookId);
}
