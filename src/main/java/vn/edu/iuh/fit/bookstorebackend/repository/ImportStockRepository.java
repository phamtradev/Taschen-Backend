package vn.edu.iuh.fit.bookstorebackend.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.model.ImportStock;

import java.util.Optional;

@Repository
public interface ImportStockRepository extends JpaRepository<ImportStock, Long> {
    
    @EntityGraph(type = EntityGraph.EntityGraphType.FETCH, attributePaths = {"importStockDetails", "importStockDetails.book"})
    @Override
    Optional<ImportStock> findById(Long id);
}
