package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CartItemResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.Cart;
import vn.edu.iuh.fit.bookstorebackend.model.CartItem;
import vn.edu.iuh.fit.bookstorebackend.repository.CartItemRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.CartRepository;
import vn.edu.iuh.fit.bookstorebackend.service.CartItemService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;

    @Override
    @Transactional
    public CartItemResponse updateQuantity(Long cartItemId, Integer quantity) throws IdInvalidException {
        if (cartItemId == null || cartItemId <= 0) {
            throw new IdInvalidException("Cart item identifier is invalid: " + cartItemId);
        }

        if (quantity == null || quantity <= 0) {
            throw new IdInvalidException("Quantity must be greater than 0");
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with identifier: " + cartItemId));

        Book book = cartItem.getBook();
        if (book.getIsActive() == null || !book.getIsActive()) {
            throw new RuntimeException("Book is not active: " + book.getId());
        }

        if (book.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + book.getStockQuantity() + ", Requested: " + quantity);
        }

        cartItem.setQuantity(quantity);
        CartItem savedCartItem = cartItemRepository.save(cartItem);

        updateCartTotalPrice(cartItem.getCart());

        return convertToCartItemResponse(savedCartItem);
    }

    @Override
    @Transactional
    public CartItemResponse increaseQuantity(Long cartItemId) throws IdInvalidException {
        if (cartItemId == null || cartItemId <= 0) {
            throw new IdInvalidException("Cart item identifier is invalid: " + cartItemId);
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with identifier: " + cartItemId));

        Book book = cartItem.getBook();
        if (book.getIsActive() == null || !book.getIsActive()) {
            throw new RuntimeException("Book is not active: " + book.getId());
        }

        int newQuantity = cartItem.getQuantity() + 1;
        if (book.getStockQuantity() < newQuantity) {
            throw new RuntimeException("Insufficient stock. Available: " + book.getStockQuantity() + ", Requested: " + newQuantity);
        }

        cartItem.setQuantity(newQuantity);
        CartItem savedCartItem = cartItemRepository.save(cartItem);

        updateCartTotalPrice(cartItem.getCart());

        return convertToCartItemResponse(savedCartItem);
    }

    @Override
    @Transactional
    public CartItemResponse decreaseQuantity(Long cartItemId) throws IdInvalidException {
        if (cartItemId == null || cartItemId <= 0) {
            throw new IdInvalidException("Cart item identifier is invalid: " + cartItemId);
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with identifier: " + cartItemId));

        int currentQuantity = cartItem.getQuantity();
        if (currentQuantity <= 1) {
            throw new RuntimeException("Quantity cannot be decreased below 1. Use removeItem to delete the item");
        }

        cartItem.setQuantity(currentQuantity - 1);
        CartItem savedCartItem = cartItemRepository.save(cartItem);

        updateCartTotalPrice(cartItem.getCart());

        return convertToCartItemResponse(savedCartItem);
    }

    @Override
    @Transactional
    public void removeItem(Long cartItemId) throws IdInvalidException {
        if (cartItemId == null || cartItemId <= 0) {
            throw new IdInvalidException("Cart item identifier is invalid: " + cartItemId);
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with identifier: " + cartItemId));

        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);

        updateCartTotalPrice(cart);
    }

    @Override
    public CartItemResponse getItem(Long cartItemId) throws IdInvalidException {
        if (cartItemId == null || cartItemId <= 0) {
            throw new IdInvalidException("Cart item identifier is invalid: " + cartItemId);
        }

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found with identifier: " + cartItemId));

        return convertToCartItemResponse(cartItem);
    }

    private void updateCartTotalPrice(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCart(cart);
        if (items == null || items.isEmpty()) {
            cart.setTotalPrice(0.0);
        } else {
            double totalPrice = items.stream()
                    .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                    .sum();
            cart.setTotalPrice(totalPrice);
        }
        cartRepository.save(cart);
    }

    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        CartItemResponse cartItemResponse = new CartItemResponse();
        cartItemResponse.setId(cartItem.getId());
        cartItemResponse.setBookId(cartItem.getBook().getId());
        cartItemResponse.setBookTitle(cartItem.getBook().getTitle());
        cartItemResponse.setQuantity(cartItem.getQuantity());
        cartItemResponse.setUnitPrice(cartItem.getUnitPrice());
        cartItemResponse.setTotalPrice(cartItem.getTotalPrice());
        return cartItemResponse;
    }
}
