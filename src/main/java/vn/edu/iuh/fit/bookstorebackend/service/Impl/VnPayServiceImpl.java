package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookstorebackend.config.VnPayConfig;
import vn.edu.iuh.fit.bookstorebackend.service.VnPayService;
import vn.edu.iuh.fit.bookstorebackend.util.VnPayUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VnPayServiceImpl implements VnPayService {

    private final VnPayConfig vnPayConfig;

    @Override
    public String createPaymentUrl(Long orderId, double amount, HttpServletRequest request) {

        String vnp_Version = vnPayConfig.getVersion();
        String vnp_Command = vnPayConfig.getCommand();
        String vnp_TmnCode = vnPayConfig.getTmnCode();
        String vnp_CurrCode = vnPayConfig.getCurrCode();
        String vnp_TxnRef = orderId.toString();
        String vnp_OrderInfo = "Thanh_toan_don_hang_" + orderId;
        String vnp_OrderType = vnPayConfig.getOrderType();
        String vnp_Locale = vnPayConfig.getLocale();
        String vnp_ReturnUrl = vnPayConfig.getReturnUrl();
        String vnp_IpAddr = "127.0.0.1";
        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String vnp_Amount = String.valueOf((long) (amount * 100));

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String secretKey = vnPayConfig.getSecretKey();
        if (secretKey == null || secretKey.trim().isEmpty()) {
            throw new IllegalStateException("VNPay Secret Key is not configured");
        }
        secretKey = secretKey.trim();
        
        String hashDataString = hashData.toString();
        log.debug("VNPay Hash Data: {}", hashDataString);
        
        String secureHash = VnPayUtil.hmacSHA512(secretKey, hashDataString);
        log.debug("VNPay Secure Hash: {}", secureHash);

        String paymentUrl = vnPayConfig.getPaymentUrl()
                + "?"
                + query
                + "&vnp_SecureHash="
                + secureHash;

        return paymentUrl;
    }

    @Override
    public Map<String, String> processPaymentReturn(Map<String, String> params) {
        log.info("VNPay Return Params received (raw): {}", params);
        log.info("VNPay Return Params size: {}", params.size());
        
        if (params == null || params.isEmpty()) {
            log.error("VNPay Return Params is null or empty");
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("status", "failed");
            errorResult.put("message", "Không nhận được dữ liệu từ VNPay");
            return errorResult;
        }
        
        Map<String, String> paramsCopy = new HashMap<>(params);
        String vnp_SecureHash = paramsCopy.remove("vnp_SecureHash");
        String vnp_SecureHashType = paramsCopy.remove("vnp_SecureHashType");
        
        log.info("VNPay SecureHash from params: {}", vnp_SecureHash);
        log.info("VNPay SecureHashType: {}", vnp_SecureHashType);
        log.info("VNPay Params after removing hash fields: {}", paramsCopy);
        
        if (vnp_SecureHash == null || vnp_SecureHash.isEmpty()) {
            log.error("VNPay SecureHash is missing in params");
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("status", "failed");
            errorResult.put("message", "Thiếu chữ ký từ VNPay");
            return errorResult;
        }
        
        String secretKey = vnPayConfig.getSecretKey();
        if (secretKey == null || secretKey.trim().isEmpty()) {
            log.error("VNPay Secret Key is not configured");
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("status", "failed");
            errorResult.put("message", "Secret key không được cấu hình");
            return errorResult;
        }
        secretKey = secretKey.trim();

        List<String> fieldNames = new ArrayList<>(paramsCopy.keySet());
        Collections.sort(fieldNames);
        
        log.info("VNPay Sorted field names: {}", fieldNames);

        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = paramsCopy.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8));
            }
        }
        
        String hashDataString = hashData.toString();
        log.info("VNPay Return Hash Data String: {}", hashDataString);
        log.info("VNPay Return Secure Hash from VNPay: {}", vnp_SecureHash);
        log.info("VNPay Secret Key length: {}", secretKey.length());

        if (hashDataString.isEmpty()) {
            log.error("Hash data string is empty! Params: {}", paramsCopy);
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("status", "failed");
            errorResult.put("message", "Không có dữ liệu để xác thực");
            return errorResult;
        }

        String signValue = VnPayUtil.hmacSHA512(secretKey, hashDataString);
        log.info("VNPay Return Calculated Hash: {}", signValue);
        log.info("Hash comparison - Equal: {}", signValue != null && signValue.equals(vnp_SecureHash));

        Map<String, String> result = new HashMap<>();

        if (signValue != null && signValue.equals(vnp_SecureHash)) {
            String vnp_ResponseCode = paramsCopy.get("vnp_ResponseCode");
            log.info("VNPay Response Code: {}", vnp_ResponseCode);
            
            if ("00".equals(vnp_ResponseCode)) {
                result.put("status", "success");
                result.put("message", "Thanh toán thành công");
            } else {
                result.put("status", "failed");
                result.put("message", "Thanh toán thất bại với mã: " + vnp_ResponseCode);
            }
            result.put("vnp_TxnRef", paramsCopy.get("vnp_TxnRef"));
            result.put("vnp_Amount", paramsCopy.get("vnp_Amount"));
            result.put("vnp_ResponseCode", vnp_ResponseCode);
            result.put("vnp_TransactionNo", paramsCopy.get("vnp_TransactionNo"));
            result.put("vnp_OrderInfo", paramsCopy.get("vnp_OrderInfo"));
        } else {
            log.error("VNPay signature verification failed!");
            log.error("Expected hash from VNPay: {}", vnp_SecureHash);
            log.error("Calculated hash: {}", signValue);
            log.error("Hash data used: {}", hashDataString);
            result.put("status", "failed");
            result.put("message", "Chữ ký không hợp lệ");
        }

        return result;
    }
}
