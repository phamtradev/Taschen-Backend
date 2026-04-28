package vn.edu.iuh.fit.bookstorebackend.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.user.model.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
}








