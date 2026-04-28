package vn.edu.iuh.fit.bookstorebackend.marketing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.marketing.model.Banner;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
}
