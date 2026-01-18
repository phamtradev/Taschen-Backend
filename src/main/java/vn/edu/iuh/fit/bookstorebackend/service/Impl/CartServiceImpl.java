package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.AddToCartRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateCartItemRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CartItemResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CartResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.Cart;
import vn.edu.iuh.fit.bookstorebackend.model.CartItem;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.CartItemRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.CartRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.CartService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public CartResponse getCartByAccount(Long userId) throws IdInvalidException {
        if (userId == null || userId <= 0) {
            throw new IdInvalidException("User identifier is invalid: " + userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalPrice(0.0);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        // Fetch items to ensure they are loaded
        List<CartItem> items = cartItemRepository.findByCart(cart);
        cart.setItems(items != null ? items : new ArrayList<>());

        return convertToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) throws IdInvalidException {
        if (userId == null || userId <= 0) {
            throw new IdInvalidException("User identifier is invalid: " + userId);
        }

        if (request == null) {
            throw new IdInvalidException("AddToCartRequest cannot be null");
        }

        if (request.getBookId() == null || request.getBookId() <= 0) {
            throw new IdInvalidException("Book identifier is invalid: " + request.getBookId());
        }

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IdInvalidException("Quantity must be greater than 0");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + userId));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + request.getBookId()));

        if (book.getIsActive() == null || !book.getIsActive()) {
            throw new RuntimeException("Book is not active: " + request.getBookId());
        }

        if (book.getStockQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock. Available: " + book.getStockQuantity() + ", Requested: " + request.getQuantity());
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalPrice(0.0);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        CartItem existingCartItem = cartItemRepository.findByCartAndBookId(cart, request.getBookId())
                .orElse(null);

        if (existingCartItem != null) {
            int newQuantity = existingCartItem.getQuantity() + request.getQuantity();
            if (book.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Insufficient stock. Available: " + book.getStockQuantity() + ", Requested: " + newQuantity);
            }
            existingCartItem.setQuantity(newQuantity);
            cartItemRepository.save(existingCartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setBook(book);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUnitPrice(book.getPrice());
            cartItemRepository.save(cartItem);
        }

        updateCartTotalPrice(cart);
        Cart updatedCart = cartRepository.save(cart);
        return convertToCartResponse(updatedCart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) throws IdInvalidException {
        if (userId == null || userId <= 0) {
            throw new IdInvalidException("User identifier is invalid: " + userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        cartItemRepository.deleteByCart(cart);
        cart.setTotalPrice(0.0);
        cart.setItems(new ArrayList<>());
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public CartResponse checkout(Long userId) throws IdInvalidException {
        if (userId == null || userId <= 0) {
            throw new IdInvalidException("User identifier is invalid: " + userId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + userId));

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        // Fetch items to ensure they are loaded
        List<CartItem> items = cartItemRepository.findByCart(cart);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot checkout");
        }

        // Validate stock for all items
        for (CartItem item : items) {
            Book book = item.getBook();
            if (book.getIsActive() == null || !book.getIsActive()) {
                throw new RuntimeException("Book is not active: " + book.getId());
            }
            if (book.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for book: " + book.getTitle() + ". Available: " + book.getStockQuantity() + ", Requested: " + item.getQuantity());
            }
        }

        // Update stock quantities
        for (CartItem item : items) {
            Book book = item.getBook();
            book.setStockQuantity(book.getStockQuantity() - item.getQuantity());
            bookRepository.save(book);
        }

        // Clear cart after checkout
        cartItemRepository.deleteByCart(cart);
        cart.setTotalPrice(0.0);
        cart.setItems(new ArrayList<>());
        Cart clearedCart = cartRepository.save(cart);

        return convertToCartResponse(clearedCart);
    }

    private void updateCartTotalPrice(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCart(cart);
        if (items == null || items.isEmpty()) {
            cart.setTotalPrice(0.0);
            cart.setItems(new ArrayList<>());
            return;
        }

        double totalPrice = items.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
        cart.setTotalPrice(totalPrice);
        cart.setItems(items);
    }

    private CartResponse convertToCartResponse(Cart cart) {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setId(cart.getId());
        cartResponse.setUserId(cart.getUser().getId());
        cartResponse.setTotalPrice(cart.getTotalPrice());

        if (cart.getItems() != null) {
            List<CartItemResponse> cartItemResponses = cart.getItems().stream()
                    .map(this::convertToCartItemResponse)
                    .collect(Collectors.toList());
            cartResponse.setItems(cartItemResponses);
        } else {
            cartResponse.setItems(new ArrayList<>());
        }

        return cartResponse;
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
