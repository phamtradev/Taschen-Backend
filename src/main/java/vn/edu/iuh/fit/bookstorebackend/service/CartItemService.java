package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.response.CartItemResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

public interface CartItemService {
    
    CartItemResponse updateQuantity(Long cartItemId, Integer quantity) throws IdInvalidException;
    
    CartItemResponse increaseQuantity(Long cartItemId) throws IdInvalidException;
    
    CartItemResponse decreaseQuantity(Long cartItemId) throws IdInvalidException;
    
    void removeItem(Long cartItemId) throws IdInvalidException;
    
    CartItemResponse getItem(Long cartItemId) throws IdInvalidException;
}
