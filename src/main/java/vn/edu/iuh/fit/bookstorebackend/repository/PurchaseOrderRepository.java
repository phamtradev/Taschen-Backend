package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.PurchaseOrder;

import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"purchaseOrderItems", "purchaseOrderItems.book"})
    @Override
    Optional<PurchaseOrder> findById(Long id);
}
