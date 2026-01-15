package vn.edu.iuh.fit.bookstorebackend.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@example.com}")
    private String emailFrom;

    @Value("${app.mail.from-name:}")
    private String fromName;

    @Value("${app.frontend.verify-url:http://localhost:8080/api/auth/verify?token=}")
    private String verifyUrlPrefix;

    public void sendVerificationEmail(String to, String token) throws MessagingException {
        String verifyLink = verifyUrlPrefix + token;
        String html = "<!doctype html>"
                + "<html><body style=\"font-family:Arial,sans-serif;color:#333;\">"
                + "<h3>Please verify your account</h3>"
                + "<p>Click the button below to verify your email address and activate your account.</p>"
                + "<div style=\"text-align:center;margin:30px 0;\">"
                + "<a href=\"" + verifyLink + "\" "
                + "style=\"display:inline-block;padding:12px 24px;font-size:16px;color:#fff;background-color:#1a73e8;"
                + "text-decoration:none;border-radius:6px;\">Verify account</a>"
                + "</div>"
                + "<p>If the button doesn't work, copy and paste this link into your browser:</p>"
                + "<p><a href=\"" + verifyLink + "\">" + verifyLink + "</a></p>"
                + "<hr/><p>Thank you.</p>"
                + "</body></html>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        try {
            if (fromName != null && !fromName.isBlank()) {
                helper.setFrom(emailFrom, fromName);
            } else {
                helper.setFrom(emailFrom);
            }
        } catch (Exception e) {
            helper.setFrom(emailFrom);
        }

        helper.setTo(to);
        helper.setSubject("Verify your account");
        helper.setText(html, true);

        mailSender.send(message);
        log.info("Verification email sent to {}", to);
    }
}


