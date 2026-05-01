package vn.edu.iuh.fit.bookstorebackend.book.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.book.model.Book;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByTitle(String title);

    Optional<Book> findByIdAndIsActiveNotNull(Long id);

    List<Book> findByCategories_Id(Long categoryId);

    @Query("SELECT DISTINCT b FROM Book b JOIN FETCH b.categories c WHERE c.id = :categoryId")
    List<Book> findByCategoryIdWithCategories(@Param("categoryId") Long categoryId);

    List<Book> findBySupplierId(Long supplierId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Book b " +
           "JOIN b.bookVariants bv WHERE b.title = :title AND bv.variant.id IN :variantIds")
    boolean existsByTitleAndVariantIds(@Param("title") String title, @Param("variantIds") List<Long> variantIds);

    @Query("""
            SELECT b FROM Book b
            LEFT JOIN FETCH b.categories c
            WHERE (:status = 'all'
                   OR (:status = 'active' AND b.isActive IS NOT NULL)
                   OR (:status = 'deleted' AND b.isActive IS NULL))
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR c.id = :categoryId)
            ORDER BY
                CASE WHEN :sortBy = 'category' THEN c.name END ASC,
                CASE WHEN :sortBy = 'category_desc' THEN c.name END DESC,
                CASE WHEN :sortBy = 'title' THEN b.title END ASC,
                CASE WHEN :sortBy = 'title_desc' THEN b.title END DESC,
                CASE WHEN :sortBy = 'price' THEN b.price END ASC,
                CASE WHEN :sortBy = 'price_desc' THEN b.price END DESC,
                CASE WHEN :sortBy = 'id' OR :sortBy IS NULL THEN b.id END ASC
            """)
    List<Book> searchBooks(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("sortBy") String sortBy,
            @Param("status") String status);

    @Query(value = """
            SELECT b FROM Book b
            LEFT JOIN b.categories c
            WHERE (:status = 'all'
                   OR (:status = 'active' AND b.isActive IS NOT NULL)
                   OR (:status = 'deleted' AND b.isActive IS NULL))
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR c.id = :categoryId)
            """,
            countQuery = """
            SELECT COUNT(DISTINCT b) FROM Book b
            LEFT JOIN b.categories c
            WHERE (:status = 'all'
                   OR (:status = 'active' AND b.isActive IS NOT NULL)
                   OR (:status = 'deleted' AND b.isActive IS NULL))
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR c.id = :categoryId)
            """)
    Page<Book> searchBooks(@Param("keyword") String keyword,
                           @Param("categoryId") Long categoryId,
                           @Param("status") String status,
                           Pageable pageable);
}
