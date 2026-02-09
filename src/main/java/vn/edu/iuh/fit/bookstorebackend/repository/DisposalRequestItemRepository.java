package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.DisposalRequestItem;

import java.util.List;

@Repository
public interface DisposalRequestItemRepository extends JpaRepository<DisposalRequestItem, Long> {
    List<DisposalRequestItem> findByDisposalRequest_Id(Long disposalRequestId);
}
