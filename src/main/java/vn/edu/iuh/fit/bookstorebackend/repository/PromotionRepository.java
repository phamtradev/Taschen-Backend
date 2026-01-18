package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.common.PromotionStatus;
import vn.edu.iuh.fit.bookstorebackend.model.Promotion;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    Optional<Promotion> findByCode(String code);

    boolean existsByCode(String code);

    List<Promotion> findByStatus(PromotionStatus status);

    List<Promotion> findByIsActive(Boolean isActive);

    @Query("SELECT p FROM Promotion p WHERE " +
            "(:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:code IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :code, '%'))) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:isActive IS NULL OR p.isActive = :isActive)")
    List<Promotion> searchPromotions(
            @Param("name") String name,
            @Param("code") String code,
            @Param("status") PromotionStatus status,
            @Param("isActive") Boolean isActive
    );

    @Query("SELECT p FROM Promotion p WHERE p.status = 'ACTIVE' AND p.isActive = true " +
            "AND CURRENT_DATE BETWEEN p.startDate AND p.endDate")
    List<Promotion> findActivePromotions();
}
