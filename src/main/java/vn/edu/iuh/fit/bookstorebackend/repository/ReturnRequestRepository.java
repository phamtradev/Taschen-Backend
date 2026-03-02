package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.ReturnRequest;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {

    //chi query 1 lan thoi, ko query tung field rieng biet
    //neu ko dung thi se dan den loi N + 1 query
    /*
        Query 1: SELECT * FROM return_request WHERE id = 1
        Query 2: SELECT * FROM orders WHERE order_id = ?
        Query 3: SELECT * FROM order_details WHERE order_id = ?
        Query 4: SELECT * FROM books WHERE id = ?
        Query 5: SELECT * FROM users WHERE id = ?
    */
   //neu dung cai nay thi se query tat ca cac field cua order, orderDetails, book, createdBy, processedBy (only one query)
   /*
        Query 1: SELECT * FROM return_request rr
         JOIN orders o ON rr.order_id = o.id
         JOIN order_details od ON o.id = od.order_id
         JOIN books b ON od.book_id = b.id
         JOIN users u1 ON rr.created_by = u1.id
         LEFT JOIN users u2 ON rr.processed_by = u2.id
         WHERE rr.id = 1
   */
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"order", "order.orderDetails", "order.orderDetails.book", "createdBy", "processedBy"})
    @Override
    Optional<ReturnRequest> findById(Long id);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"order", "order.orderDetails", "order.orderDetails.book", "createdBy", "processedBy"})
    List<ReturnRequest> findByCreatedBy_IdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"order", "order.orderDetails", "order.orderDetails.book", "createdBy", "processedBy"})
    List<ReturnRequest> findAllByOrderByCreatedAtDesc();

    boolean existsByOrder_Id(Long orderId);
}
