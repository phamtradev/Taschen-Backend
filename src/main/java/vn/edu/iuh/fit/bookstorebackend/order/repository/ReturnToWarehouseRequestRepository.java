package vn.edu.iuh.fit.bookstorebackend.order.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.order.model.ReturnToWarehouseRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnToWarehouseRequestRepository extends JpaRepository<ReturnToWarehouseRequest, Long> {

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"book", "createdBy", "processedBy"})
    @Override
    Optional<ReturnToWarehouseRequest> findById(Long id);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"book", "createdBy", "processedBy"})
    List<ReturnToWarehouseRequest> findByCreatedBy_IdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"book", "createdBy", "processedBy"})
    List<ReturnToWarehouseRequest> findAllByOrderByCreatedAtDesc();
}
