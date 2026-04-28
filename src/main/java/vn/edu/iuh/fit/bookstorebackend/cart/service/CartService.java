package vn.edu.iuh.fit.bookstorebackend.cart.service;

import vn.edu.iuh.fit.bookstorebackend.cart.dto.request.AddToCartRequest;
import vn.edu.iuh.fit.bookstorebackend.cart.dto.response.CartResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

public interface CartService {

    CartResponse getCartByAccount(Long userId) throws IdInvalidException;

    CartResponse getCartByCurrentUser() throws IdInvalidException;

    CartResponse addToCart(Long userId, AddToCartRequest request) throws IdInvalidException;

    void clearCart(Long userId) throws IdInvalidException;

    CartResponse checkout(Long userId) throws IdInvalidException;

    CartResponse checkoutCurrentUser() throws IdInvalidException;
}
