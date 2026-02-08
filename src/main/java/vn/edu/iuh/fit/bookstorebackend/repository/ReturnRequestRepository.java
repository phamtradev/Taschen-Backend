package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.ReturnRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"order", "createdBy", "processedBy"})
    @Override
    Optional<ReturnRequest> findById(Long id);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"order", "createdBy", "processedBy"})
    List<ReturnRequest> findByCreatedBy_IdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"order", "createdBy", "processedBy"})
    List<ReturnRequest> findAllByOrderByCreatedAtDesc();

    boolean existsByOrder_Id(Long orderId);
}
