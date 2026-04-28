package vn.edu.iuh.fit.bookstorebackend.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.cart.model.Cart;
import vn.edu.iuh.fit.bookstorebackend.user.model.User;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
    
    Optional<Cart> findByUserId(Long userId);
}
