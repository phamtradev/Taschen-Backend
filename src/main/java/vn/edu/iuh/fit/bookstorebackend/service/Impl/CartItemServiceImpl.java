package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CartItemResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.Cart;
import vn.edu.iuh.fit.bookstorebackend.model.CartItem;
import vn.edu.iuh.fit.bookstorebackend.mapper.CartItemMapper;
import vn.edu.iuh.fit.bookstorebackend.repository.CartItemRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.CartRepository;
import vn.edu.iuh.fit.bookstorebackend.service.CartItemService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final CartItemMapper cartItemMapper;

    @Override
    @Transactional
    public CartItemResponse updateQuantity(Long cartItemId, Integer quantity) throws IdInvalidException {
        validateCartItemId(cartItemId);
        validateQuantity(quantity);
        
        CartItem cartItem = findCartItemById(cartItemId);
        validateBookForQuantity(cartItem.getBook(), quantity);
        
        updateCartItemQuantity(cartItem, quantity);
        CartItem savedCartItem = cartItemRepository.save(cartItem);
        updateCartTotalPrice(cartItem.getCart());
        
        return cartItemMapper.toCartItemResponse(savedCartItem);
    }
    
    private void validateCartItemId(Long cartItemId) throws IdInvalidException {
        if (cartItemId == null || cartItemId <= 0) {
            throw new IdInvalidException("Cart item identifier is invalid: " + cartItemId);
        }
    }
    
    private void validateQuantity(Integer quantity) throws IdInvalidException {
        if (quantity == null || quantity <= 0) {
            throw new IdInvalidException("Quantity must be greater than 0");
        }
    }
    
    private CartItem findCartItemById(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with identifier: " + cartItemId));
    }
    
    private void validateBookForQuantity(Book book, Integer quantity) {
        if (book.getIsActive() == null || !book.getIsActive()) {
            throw new RuntimeException("Book is not active: " + book.getId());
        }
        if (book.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + book.getStockQuantity() + ", Requested: " + quantity);
        }
    }
    
    private void updateCartItemQuantity(CartItem cartItem, Integer quantity) {
        cartItem.setQuantity(quantity);
    }

    @Override
    @Transactional
    public CartItemResponse increaseQuantity(Long cartItemId) throws IdInvalidException {
        validateCartItemId(cartItemId);
        CartItem cartItem = findCartItemById(cartItemId);
        
        int newQuantity = cartItem.getQuantity() + 1;
        validateBookForQuantity(cartItem.getBook(), newQuantity);
        
        updateCartItemQuantity(cartItem, newQuantity);
        CartItem savedCartItem = cartItemRepository.save(cartItem);
        updateCartTotalPrice(cartItem.getCart());
        
        return cartItemMapper.toCartItemResponse(savedCartItem);
    }

    @Override
    @Transactional
    public CartItemResponse decreaseQuantity(Long cartItemId) throws IdInvalidException {
        validateCartItemId(cartItemId);
        CartItem cartItem = findCartItemById(cartItemId);
        
        validateQuantityCanBeDecreased(cartItem.getQuantity());
        
        int newQuantity = cartItem.getQuantity() - 1;
        updateCartItemQuantity(cartItem, newQuantity);
        CartItem savedCartItem = cartItemRepository.save(cartItem);
        updateCartTotalPrice(cartItem.getCart());
        
        return cartItemMapper.toCartItemResponse(savedCartItem);
    }
    
    private void validateQuantityCanBeDecreased(int currentQuantity) {
        if (currentQuantity <= 1) {
            throw new RuntimeException("Quantity cannot be decreased below 1. Use removeItem to delete the item");
        }
    }

    @Override
    @Transactional
    public void removeItem(Long cartItemId) throws IdInvalidException {
        validateCartItemId(cartItemId);
        CartItem cartItem = findCartItemById(cartItemId);
        
        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);
        updateCartTotalPrice(cart);
    }

    @Override
    public CartItemResponse getItem(Long cartItemId) throws IdInvalidException {
        validateCartItemId(cartItemId);
        CartItem cartItem = findCartItemById(cartItemId);
        return cartItemMapper.toCartItemResponse(cartItem);
    }

    private void updateCartTotalPrice(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCart(cart);
        double totalPrice = calculateCartTotalPrice(items);
        cart.setTotalPrice(totalPrice);
        cartRepository.save(cart);
    }
    
    private double calculateCartTotalPrice(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        return items.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }
}
