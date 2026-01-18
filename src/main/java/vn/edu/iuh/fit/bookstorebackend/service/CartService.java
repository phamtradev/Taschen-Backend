package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.AddToCartRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateCartItemRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CartResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

public interface CartService {
    
    CartResponse getCartByAccount(Long userId) throws IdInvalidException;
    
    CartResponse addToCart(Long userId, AddToCartRequest request) throws IdInvalidException;
    
    CartResponse updateCartItem(Long cartItemId, UpdateCartItemRequest request) throws IdInvalidException;
    
    CartResponse removeCartItem(Long cartItemId) throws IdInvalidException;
    
    void clearCart(Long userId) throws IdInvalidException;
    
    CartResponse checkout(Long userId) throws IdInvalidException;
}
