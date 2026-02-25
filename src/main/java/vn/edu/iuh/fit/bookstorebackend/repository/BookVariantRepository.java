package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.BookVariant;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookVariantRepository extends JpaRepository<BookVariant, Long> {

    List<BookVariant> findByBookId(Long bookId);

    List<BookVariant> findByVariantId(Long variantId);

    Optional<BookVariant> findByBookIdAndVariantId(Long bookId, Long variantId);

    boolean existsByBookIdAndVariantId(Long bookId, Long variantId);

    void deleteByBookId(Long bookId);
}
