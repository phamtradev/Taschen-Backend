package vn.edu.iuh.fit.bookstorebackend.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.user.model.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    @Query("SELECT p FROM Permission p WHERE " +
           "(:keyword IS NULL OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.pathPattern) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Permission> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
}








