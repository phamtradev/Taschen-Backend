package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.AddToCartRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CartResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.CartService;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/current")
    public ResponseEntity<CartResponse> getCartByCurrentUser() throws IdInvalidException {
        CartResponse cartResponse = cartService.getCartByCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(cartResponse);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<CartResponse> getCartByAccount(@PathVariable Long userId) throws IdInvalidException {
        CartResponse cartResponse = cartService.getCartByAccount(userId);
        return ResponseEntity.status(HttpStatus.OK).body(cartResponse);
    }

    @PostMapping("/users/{userId}/items")
    public ResponseEntity<CartResponse> addToCart(
            @PathVariable Long userId,
            @RequestBody AddToCartRequest request) throws IdInvalidException {
        CartResponse cartResponse = cartService.addToCart(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartResponse);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) throws IdInvalidException {
        cartService.clearCart(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/current/checkout")
    public ResponseEntity<CartResponse> checkoutCurrentUser() throws IdInvalidException {
        CartResponse cartResponse = cartService.checkoutCurrentUser();
        return ResponseEntity.status(HttpStatus.OK).body(cartResponse);
    }

    @PostMapping("/users/{userId}/checkout")
    public ResponseEntity<CartResponse> checkout(@PathVariable Long userId) throws IdInvalidException {
        CartResponse cartResponse = cartService.checkout(userId);
        return ResponseEntity.status(HttpStatus.OK).body(cartResponse);
    }
}
