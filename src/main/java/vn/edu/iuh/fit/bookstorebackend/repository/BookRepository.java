package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.Book;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByTitle(String title);
    
    List<Book> findByCategories_Id(Long categoryId);
    
    @Query("SELECT DISTINCT b FROM Book b JOIN FETCH b.categories c WHERE c.id = :categoryId")
    List<Book> findByCategoryIdWithCategories(@Param("categoryId") Long categoryId);
    
    List<Book> findBySupplierId(Long supplierId);
}
