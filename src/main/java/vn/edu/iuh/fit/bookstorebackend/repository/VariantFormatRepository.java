package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.VariantFormat;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface VariantFormatRepository extends JpaRepository<VariantFormat, Long> {
    Set<VariantFormat> findByIdIn(List<Long> ids);
    
    Optional<VariantFormat> findByCode(String code);
    
    boolean existsByCode(String code);
}
