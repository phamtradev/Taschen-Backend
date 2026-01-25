package vn.edu.iuh.fit.bookstorebackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VnPayConfig {
    private String tmnCode;
    private String secretKey;
    private String paymentUrl;
    private String returnUrl;
    private String version;
    private String command;
    private String orderType;
    private String locale;
    private String currCode;
    private String createOrderUrl;
    private String queryDrUrl;
    private String refundUrl;
}
