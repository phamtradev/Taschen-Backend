package vn.edu.iuh.fit.bookstorebackend.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.book.model.BookEmbedding;

import java.util.Optional;

@Repository
public interface BookEmbeddingRepository extends JpaRepository<BookEmbedding, Long> {
    Optional<BookEmbedding> findByBookId(Long bookId);

    void deleteByBookId(Long bookId);

    boolean existsByBookId(Long bookId);

    Page<BookEmbedding> findAll(Pageable pageable);
}
