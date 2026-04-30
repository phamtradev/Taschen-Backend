package vn.edu.iuh.fit.bookstorebackend.user.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.user.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"roles.permissions", "addresses"})
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions LEFT JOIN FETCH u.addresses")
    List<User> findAllWithRoles();

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions LEFT JOIN FETCH u.addresses WHERE u.id = :id")
    Optional<User> findByIdWithRoles(Long id);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.code = :roleCode AND u.isActive = true")
    List<User> findAllActiveByRoleCode(@Param("roleCode") String roleCode);
}

