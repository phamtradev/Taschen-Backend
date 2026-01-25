package vn.edu.iuh.fit.bookstorebackend.service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VnPayService {

    String createPaymentUrl(Long orderId, double amount, HttpServletRequest request);

    Map<String, String> processPaymentReturn(Map<String, String> params);
    
}
