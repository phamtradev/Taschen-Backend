package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.BatchDetail;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatchDetailRepository extends JpaRepository<BatchDetail, Long> {
    
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"batch", "orderDetail", "orderDetail.order", "orderDetail.book"})
    @Override
    Optional<BatchDetail> findById(Long id);

    List<BatchDetail> findByBatch_IdOrderByIdDesc(Long batchId);

    List<BatchDetail> findByOrderDetail_Id(Long orderDetailId);

    List<BatchDetail> findByOrderDetail_Order_Id(Long orderId);
}
