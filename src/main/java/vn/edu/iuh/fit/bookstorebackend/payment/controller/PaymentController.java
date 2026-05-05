package vn.edu.iuh.fit.bookstorebackend.payment.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.order.dto.response.OrderResponse;
import vn.edu.iuh.fit.bookstorebackend.order.service.OrderService;
import vn.edu.iuh.fit.bookstorebackend.payment.config.VnPayConfig;
import vn.edu.iuh.fit.bookstorebackend.payment.service.VnPayService;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final VnPayService vnPayService;
    private final OrderService orderService;
    private final VnPayConfig vnPayConfig;

    @PostMapping("/vnpay/create/{orderId}")
    public ResponseEntity<Map<String, String>> createVnPayPayment(
            @PathVariable Long orderId,
            HttpServletRequest request) throws IdInvalidException {
        OrderResponse order = orderService.getOrderById(orderId);
        String paymentUrl = vnPayService.createPaymentUrl(
                orderId,
                order.getTotalAmount(),
                request
        );
        return ResponseEntity.ok(Map.of("paymentUrl", paymentUrl));
    }

    @GetMapping("/vnpay/return")
    public void vnPayReturn(
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Map<String, String> params = getParamsFromRequest(request);
        Map<String, String> result = vnPayService.processPaymentReturn(params);

        if ("success".equals(result.get("status"))) {
            String vnp_TxnRef = result.get("vnp_TxnRef");
            if (vnp_TxnRef != null && !vnp_TxnRef.isEmpty()) {
                try {
                    Long orderId = Long.parseLong(vnp_TxnRef);
                    String vnp_TransactionNo = result.get("vnp_TransactionNo");
                    orderService.updatePaymentFromVnPayCallback(orderId, vnp_TransactionNo);
                } catch (NumberFormatException e) {
                    result.put("status", "failed");
                    result.put("message", "Mã giao dịch không hợp lệ: " + vnp_TxnRef);
                } catch (Exception e) {
                    result.put("status", "failed");
                    result.put("message", "Lỗi cập nhật đơn hàng: " + e.getMessage());
                }
            }
        }

        String frontendUrl = vnPayConfig.getFrontendRedirectUrl();
        String redirectUrl = frontendUrl
                + "?status=" + URLEncoder.encode(result.get("status") == null ? "failed" : result.get("status"), StandardCharsets.UTF_8)
                + "&orderId=" + (result.get("vnp_TxnRef") == null ? "" : URLEncoder.encode(result.get("vnp_TxnRef"), StandardCharsets.UTF_8))
                + "&message=" + URLEncoder.encode(result.get("message") == null ? "" : result.get("message"), StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
    
    private Map<String, String> getParamsFromRequest(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        String queryString = request.getQueryString();
        
        if (queryString != null && !queryString.isEmpty()) {
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    try {
                        String key = java.net.URLDecoder.decode(keyValue[0], java.nio.charset.StandardCharsets.UTF_8);
                        String value = java.net.URLDecoder.decode(keyValue[1], java.nio.charset.StandardCharsets.UTF_8);
                        params.put(key, value);
                    } catch (Exception e) {
                        params.put(keyValue[0], keyValue[1]);
                    }
                } else if (keyValue.length == 1) {
                    params.put(keyValue[0], "");
                }
            }
        }
        
        return params;
    }
}
