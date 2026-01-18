package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.AddToCartRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.CartResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

public interface CartService {

    CartResponse getCartByAccount(Long userId) throws IdInvalidException;

    CartResponse getCartByCurrentUser() throws IdInvalidException;

    CartResponse addToCart(Long userId, AddToCartRequest request) throws IdInvalidException;

    void clearCart(Long userId) throws IdInvalidException;

    CartResponse checkout(Long userId) throws IdInvalidException;

    CartResponse checkoutCurrentUser() throws IdInvalidException;
}
