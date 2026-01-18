package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.Variant;

import java.util.List;

@Repository
public interface VariantRepository extends JpaRepository<Variant, Long> {
    List<Variant> findByBook(Book book);
    
    List<Variant> findByBookId(Long bookId);
}
