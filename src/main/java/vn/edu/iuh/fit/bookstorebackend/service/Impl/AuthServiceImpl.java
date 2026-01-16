package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.*;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AuthenticationResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.RefreshTokenResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.RegisterResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.UserResponse;
import vn.edu.iuh.fit.bookstorebackend.model.RefreshToken;
import vn.edu.iuh.fit.bookstorebackend.model.Role;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.model.VerificationToken;
import vn.edu.iuh.fit.bookstorebackend.repository.RefreshTokenRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.RoleRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.VerificationTokenRepository;
import vn.edu.iuh.fit.bookstorebackend.service.AuthService;
import vn.edu.iuh.fit.bookstorebackend.util.JwtService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import vn.edu.iuh.fit.bookstorebackend.util.MailService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.Collections;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;
    private final Environment environment;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // no username required; use email as login identifier
        if (userRepository.existsByEmail(request.getEmail())) {
            // Check if existing user is inactive and has expired verification token
            User existingUser = userRepository.findByEmail(request.getEmail()).get();
            if (!existingUser.isActive()) {
                // Find verification token for this user
                List<VerificationToken> tokens = verificationTokenRepository.findByUser(existingUser);
                boolean hasValidToken = tokens.stream()
                    .anyMatch(token -> token.getExpiresAt().isAfter(Instant.now()));

                // If no valid tokens (all expired or revoked, or no tokens at all), allow re-registration
                if (!hasValidToken) {
                    // Delete all tokens and user
                    verificationTokenRepository.deleteByUser(existingUser);
                    userRepository.delete(existingUser);
                    log.info("Deleted inactive user with expired/revoked tokens: {}", request.getEmail());
                } else {
                    throw new RuntimeException("Email already exists: " + request.getEmail());
                }
            } else {
                throw new RuntimeException("Email already exists: " + request.getEmail());
            }
        }

        if (request.getPassword() == null || request.getConfirmPassword() == null || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password and confirm password do not match");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(false);

        // assign default USER role
        Role userRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        User saved = userRepository.save(user);

        // create verification token and send email
        String token = UUID.randomUUID().toString();
        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(saved);
        vt.setExpiresAt(Instant.now().plusSeconds(60L * 2L)); // 2 minutes for testing
        verificationTokenRepository.save(vt);
        try {
            mailService.sendVerificationEmail(saved.getEmail(), token, saved.getId());
        } catch (Exception e) {
            log.warn("Failed to send verification email: {}", e.getMessage());
        }
        RegisterResponse response = new RegisterResponse();
        response.setId(saved.getId());
        response.setEmail(saved.getEmail());
        response.setFirstName(saved.getFirstName());
        response.setLastName(saved.getLastName());
        response.setGender(saved.getGender());
        response.setPhoneNumber(saved.getPhoneNumber());
        response.setActive(saved.isActive());
        response.setRoles(saved.getRoles() == null ? null :
                saved.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toList()));

//        System.out.println("ENV MAIL_USERNAME = " + System.getenv("MAIL_USERNAME"));
//        System.out.println("ENV MAIL_PASSWORD = " + System.getenv("MAIL_PASSWORD"));
//        System.out.println("PROP spring.mail.username = " + environment.getProperty("spring.mail.username"));
//        System.out.println("PROP spring.mail.password = " + environment.getProperty("spring.mail.password"));

        return response;
    }

    @Override
    @Transactional
    public AuthenticationResponse login(AuthenticationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Account not verified");
        }

        String accessToken = jwtService.generateAccessToken(user);
        // xóa toàn bộ token cũ của người dùng để tránh trùng lặp
        refreshTokenRepository.deleteByUser(user);

        String refreshTokenStr = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(60L * 60L * 24L * 30L)); // 30 days
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        List<String> roles = user.getRoles() != null ?
                user.getRoles().stream()
                        .map(Role::getCode)
                        .collect(Collectors.toList()) :
                java.util.Collections.emptyList();

        return AuthenticationResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .userId(user.getId())
                .email(user.getEmail())
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshTokenStr = request.getRefreshToken();
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        User user = stored.getUser();

        // Revoke old refresh token
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        // Generate new access token
        String accessToken = jwtService.generateAccessToken(user);

        // Generate new refresh token
        String newRefreshTokenStr = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(newRefreshTokenStr);
        newRefreshToken.setUser(user);
        newRefreshToken.setExpiresAt(Instant.now().plusSeconds(60L * 60L * 24L * 30L)); // 30 days
        newRefreshToken.setRevoked(false);
        refreshTokenRepository.save(newRefreshToken);

        return RefreshTokenResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .refreshToken(newRefreshTokenStr)
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        Optional<RefreshToken> stored = refreshTokenRepository.findByToken(refreshToken);
        stored.ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email == null) throw new RuntimeException("Not authenticated");
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found: " + email));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }

        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            throw new RuntimeException("New password must not be empty");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void createVerificationForUser(Long userId, String token) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found: " + userId));
        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiresAt(Instant.now().plusSeconds(60L * 2L)); // 2 minutes for testing
        verificationTokenRepository.save(vt);
        try {
            mailService.sendVerificationEmail(user.getEmail(), token, user.getId());
        } catch (Exception e) {
            log.warn("Failed to send verification email: {}", e.getMessage());
        }
    }

    @Override
    public void sendPasswordResetEmail(ForgotPasswordRequest request) {
        // not implemented
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // not implemented
    }


    @Transactional
    @Override
    public void verifyEmailToken(String token) {
        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (vt.getExpiresAt().isBefore(Instant.now())) {
            // Token expired - delete user and token
            User expiredUser = vt.getUser();
            verificationTokenRepository.delete(vt);
            userRepository.delete(expiredUser);
            throw new RuntimeException("Verification token expired - account deleted. Please register again.");
        }

        User u = vt.getUser();
        u.setActive(true);
        userRepository.save(u);
        verificationTokenRepository.deleteByToken(token);
    }

    @Transactional
    @Override
    public void verifyEmailTokenForUser(Long userId, String token) {
        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (vt.getExpiresAt().isBefore(Instant.now())) {
            // Token expired - delete user and token
            User expiredUser = vt.getUser();
            verificationTokenRepository.delete(vt);
            userRepository.delete(expiredUser);
            throw new RuntimeException("Verification token expired - account deleted. Please register again.");
        }

        if (!vt.getUser().getId().equals(userId)) throw new RuntimeException("Token does not belong to user");
        User u = vt.getUser();
        u.setActive(true);
        userRepository.save(u);
        verificationTokenRepository.deleteByToken(token);
    }


}
