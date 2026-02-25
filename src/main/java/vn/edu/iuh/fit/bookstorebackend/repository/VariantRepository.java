package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantRepository extends JpaRepository<Variant, Long> {
    @Query("SELECT v FROM Variant v JOIN BookVariant bv ON v.id = bv.variant.id WHERE bv.book.id = :bookId")
    List<Variant> findByBookId(Long bookId);

    Optional<Variant> findByFormatName(String formatName);
}
