package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.DisposalRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisposalRequestRepository extends JpaRepository<DisposalRequest, Long> {

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"items", "createdBy", "processedBy", "items.batch"})
    @Override
    Optional<DisposalRequest> findById(Long id);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"items", "createdBy", "processedBy", "items.batch"})
    List<DisposalRequest> findByCreatedBy_IdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"items", "createdBy", "processedBy", "items.batch"})
    List<DisposalRequest> findAllByOrderByCreatedAtDesc();
}
