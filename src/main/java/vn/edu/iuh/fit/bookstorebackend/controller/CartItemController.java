package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateQuantityRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CartItemResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.CartItemService;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponse> getItem(@PathVariable Long cartItemId) throws IdInvalidException {
        CartItemResponse cartItemResponse = cartItemService.getItem(cartItemId);
        return ResponseEntity.status(HttpStatus.OK).body(cartItemResponse);
    }

    @PatchMapping("/{cartItemId}/increase")
    public ResponseEntity<CartItemResponse> increaseQuantity(@PathVariable Long cartItemId) throws IdInvalidException {
        CartItemResponse cartItemResponse = cartItemService.increaseQuantity(cartItemId);
        return ResponseEntity.status(HttpStatus.OK).body(cartItemResponse);
    }

    @PatchMapping("/{cartItemId}/decrease")
    public ResponseEntity<CartItemResponse> decreaseQuantity(@PathVariable Long cartItemId) throws IdInvalidException {
        CartItemResponse cartItemResponse = cartItemService.decreaseQuantity(cartItemId);
        return ResponseEntity.status(HttpStatus.OK).body(cartItemResponse);
    }

    @PutMapping("/{cartItemId}/quantity")
    public ResponseEntity<CartItemResponse> updateQuantity(
            @PathVariable Long cartItemId,
            @RequestBody UpdateQuantityRequest request) throws IdInvalidException {
        CartItemResponse cartItemResponse = cartItemService.updateQuantity(cartItemId, request.getQuantity());
        return ResponseEntity.status(HttpStatus.OK).body(cartItemResponse);
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long cartItemId) throws IdInvalidException {
        cartItemService.removeItem(cartItemId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
