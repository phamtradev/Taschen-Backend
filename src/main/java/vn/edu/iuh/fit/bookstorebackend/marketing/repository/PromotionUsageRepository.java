package vn.edu.iuh.fit.bookstorebackend.marketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.marketing.model.PromotionUsage;

@Repository
public interface PromotionUsageRepository extends JpaRepository<PromotionUsage, Long> {

    boolean existsByUserIdAndPromotionId(Long userId, Long promotionId);
}
