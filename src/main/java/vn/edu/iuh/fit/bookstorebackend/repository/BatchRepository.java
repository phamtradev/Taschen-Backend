package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.Batch;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    
  // binh thuong thi jpa se query tung field rieng biet (lazy loading), neu dung entity graph thi se query tat ca cac field cua book, createdBy, importStockDetail, batchDetails cung luc (eager loading)
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"book", "createdBy", "importStockDetail", "batchDetails"})
    @Override
    Optional<Batch> findById(Long id);

    List<Batch> findByBook_IdOrderByCreatedAtDesc(Long bookId);

    List<Batch> findByCreatedBy_IdOrderByCreatedAtDesc(Long userId);

    List<Batch> findByBook_IdAndRemainingQuantityGreaterThanOrderByCreatedAtAsc(Long bookId, int remainingQuantity);

    List<Batch> findByBook_IdAndRemainingQuantityGreaterThanOrderByCreatedAtDesc(Long bookId, int remainingQuantity);

    Optional<Batch> findByBook_IdAndVariant_IdAndImportPriceAndSupplier_Id(
            Long bookId, Long variantId, Double importPrice, Long supplierId);
}
