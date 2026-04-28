package vn.edu.iuh.fit.bookstorebackend.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.bookstorebackend.cart.model.Cart;
import vn.edu.iuh.fit.bookstorebackend.cart.model.CartItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndBookId(Cart cart, Long bookId);
    
    List<CartItem> findByCart(Cart cart);
    
    void deleteByCart(Cart cart);
}
