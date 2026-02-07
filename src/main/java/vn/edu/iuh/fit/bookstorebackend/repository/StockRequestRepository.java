package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.StockRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRequestRepository extends JpaRepository<StockRequest, Long> {
    
    //ko dung cai nay thi phai query tung field rieng biet
    //neu dung cai nay thi se query tat ca cac field cua book, createdBy, processedBy (only one query)
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"book", "createdBy", "processedBy"})
    @Override
    Optional<StockRequest> findById(Long id);
    
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"book", "createdBy", "processedBy"})
    List<StockRequest> findByCreatedBy_IdOrderByCreatedAtDesc(Long userId);
    
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"book", "createdBy", "processedBy"})
    List<StockRequest> findAllByOrderByCreatedAtDesc();
}
