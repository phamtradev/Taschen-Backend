package vn.edu.iuh.fit.bookstorebackend.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.inventory.model.ImportStock;
import vn.edu.iuh.fit.bookstorebackend.inventory.model.ImportStockDetail;

import java.util.List;

@Repository
public interface ImportStockDetailRepository extends JpaRepository<ImportStockDetail, Long> {
    
    @Query("SELECT DISTINCT isd.importStock FROM ImportStockDetail isd WHERE isd.book.id = :bookId")
    List<ImportStock> findImportStocksByBookId(@Param("bookId") Long bookId);
}
