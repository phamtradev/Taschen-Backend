package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStock;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStockDetail;

import java.util.List;

@Repository
public interface ImportStockDetailRepository extends JpaRepository<ImportStockDetail, Long> {
    
    @Query("SELECT DISTINCT isd.importStock FROM ImportStockDetail isd WHERE isd.book.id = :bookId")
    List<ImportStock> findImportStocksByBookId(@Param("bookId") Long bookId);
}
