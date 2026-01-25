package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.AddToCartRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CartResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.model.Book;
import vn.edu.iuh.fit.bookstorebackend.model.Cart;
import vn.edu.iuh.fit.bookstorebackend.model.CartItem;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.repository.BookRepository;
import vn.edu.iuh.fit.bookstorebackend.mapper.CartMapper;
import vn.edu.iuh.fit.bookstorebackend.repository.CartItemRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.CartRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.service.CartService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse getCartByAccount(Long userId) throws IdInvalidException {
        validateUserId(userId);
        User user = findUserById(userId);
        Cart cart = getOrCreateCartForUser(user);
        return cartMapper.toCartResponse(cart);
    }
    
    private void validateUserId(Long userId) throws IdInvalidException {
        if (userId == null || userId <= 0) {
            throw new IdInvalidException("User identifier is invalid: " + userId);
        }
        }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with identifier: " + userId));
    }

    @Override
    @Transactional
    public CartResponse getCartByCurrentUser() throws IdInvalidException {
        User user = getCurrentAuthenticatedUser();
        Cart cart = getOrCreateCartForUser(user);
        return cartMapper.toCartResponse(cart);
    }
    
    private User getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new RuntimeException("User is not authenticated");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    private Cart getOrCreateCartForUser(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> createNewCart(user));
    }
    
    private Cart createNewCart(User user) {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setTotalPrice(0.0);
                    return cartRepository.save(newCart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) throws IdInvalidException {
        validateUserId(userId);
        validateAddToCartRequest(request);
        
        User user = findUserById(userId);
        Book book = findAndValidateBook(request.getBookId(), request.getQuantity());
        Cart cart = getOrCreateCartForUser(user);
        
        addOrUpdateCartItem(cart, book, request.getQuantity());
        updateCartTotalPrice(cart);
        
        Cart updatedCart = cartRepository.save(cart);
        return cartMapper.toCartResponse(updatedCart);
    }
    
    private void validateAddToCartRequest(AddToCartRequest request) throws IdInvalidException {
        if (request == null) {
            throw new IdInvalidException("AddToCartRequest cannot be null");
        }
        if (request.getBookId() == null || request.getBookId() <= 0) {
            throw new IdInvalidException("Book identifier is invalid: " + request.getBookId());
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IdInvalidException("Quantity must be greater than 0");
        }
    }
    
    private Book findAndValidateBook(Long bookId, Integer quantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found with identifier: " + bookId));
        if (book.getIsActive() == null || !book.getIsActive()) {
            throw new RuntimeException("Book is not active: " + bookId);
        }
        if (book.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + book.getStockQuantity() + ", Requested: " + quantity);
        }
        return book;
    }
    
    private void addOrUpdateCartItem(Cart cart, Book book, Integer quantity) {
        CartItem existingCartItem = cartItemRepository.findByCartAndBookId(cart, book.getId())
                .orElse(null);

        if (existingCartItem != null) {
            updateExistingCartItem(existingCartItem, book, quantity);
        } else {
            createNewCartItem(cart, book, quantity);
        }
    }
    
    private void updateExistingCartItem(CartItem existingCartItem, Book book, Integer quantity) {
        int newQuantity = existingCartItem.getQuantity() + quantity;
            if (book.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Insufficient stock. Available: " + book.getStockQuantity() + ", Requested: " + newQuantity);
            }
            existingCartItem.setQuantity(newQuantity);
            cartItemRepository.save(existingCartItem);
    }
    
    private void createNewCartItem(Cart cart, Book book, Integer quantity) {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setBook(book);
        cartItem.setQuantity(quantity);
            cartItem.setUnitPrice(book.getPrice());
            cartItemRepository.save(cartItem);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) throws IdInvalidException {
        validateUserId(userId);
        User user = findUserById(userId);
        Cart cart = findCartByUser(user);
        clearCartItems(cart);
    }
    
    private Cart findCartByUser(User user) {
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + user.getId()));
    }
    
    private void clearCartItems(Cart cart) {
        cartItemRepository.deleteByCart(cart);
        cart.setTotalPrice(0.0);
        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public CartResponse checkout(Long userId) throws IdInvalidException {
        validateUserId(userId);
        User user = findUserById(userId);
        Cart cart = findCartByUser(user);
        processCheckout(cart);
        return cartMapper.toCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse checkoutCurrentUser() throws IdInvalidException {
        User user = getCurrentAuthenticatedUser();
        Cart cart = findCartByUser(user);
        processCheckout(cart);
        return cartMapper.toCartResponse(cart);
        }

    private void processCheckout(Cart cart) {
        List<CartItem> items = getCartItems(cart);
        validateCartItemsForCheckout(items);
        updateStockQuantities(items);
        clearCartItems(cart);
    }
    
    private List<CartItem> getCartItems(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCart(cart);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Cart is empty. Cannot checkout");
        }
        return items;
        }

    private void validateCartItemsForCheckout(List<CartItem> items) {
        for (CartItem item : items) {
            Book book = item.getBook();
            validateBookForCheckout(book, item.getQuantity());
        }
    }
    
    private void validateBookForCheckout(Book book, Integer quantity) {
            if (book.getIsActive() == null || !book.getIsActive()) {
                throw new RuntimeException("Book is not active: " + book.getId());
            }
        if (book.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for book: " + book.getTitle() 
                    + ". Available: " + book.getStockQuantity() + ", Requested: " + quantity);
            }
        }

    private void updateStockQuantities(List<CartItem> items) {
        for (CartItem item : items) {
            Book book = item.getBook();
            book.setStockQuantity(book.getStockQuantity() - item.getQuantity());
            bookRepository.save(book);
        }
    }

    private void updateCartTotalPrice(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCart(cart);
        if (items == null || items.isEmpty()) {
            cart.setTotalPrice(0.0);
            return;
        }

        double totalPrice = items.stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
        cart.setTotalPrice(totalPrice);
    }

}
