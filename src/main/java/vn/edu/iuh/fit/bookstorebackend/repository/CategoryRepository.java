package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.Category;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Set<Category> findByIdIn(List<Long> ids);
    
    Optional<Category> findByCode(String code);
    
    boolean existsByCode(String code);
}
