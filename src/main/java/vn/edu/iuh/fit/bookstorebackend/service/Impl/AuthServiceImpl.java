package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.iuh.fit.bookstorebackend.dto.request.AuthenticationRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ChangePasswordRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ForgotPasswordRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.RefreshTokenRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.RegisterRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ResetPasswordRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AuthenticationResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.RefreshTokenResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.RegisterResponse;
import vn.edu.iuh.fit.bookstorebackend.model.RefreshToken;
import vn.edu.iuh.fit.bookstorebackend.model.Role;
import vn.edu.iuh.fit.bookstorebackend.model.User;
import vn.edu.iuh.fit.bookstorebackend.model.VerificationToken;
import vn.edu.iuh.fit.bookstorebackend.repository.RefreshTokenRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.RoleRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.UserRepository;
import vn.edu.iuh.fit.bookstorebackend.repository.VerificationTokenRepository;
import vn.edu.iuh.fit.bookstorebackend.mapper.UserMapper;
import vn.edu.iuh.fit.bookstorebackend.service.AuthService;
import vn.edu.iuh.fit.bookstorebackend.util.JwtService;
import vn.edu.iuh.fit.bookstorebackend.util.MailService;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;


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
    private final UserMapper userMapper;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        validateEmailNotExists(request.getEmail());
        validatePasswordMatch(request);
        
        User user = createUserFromRequest(request);
        User savedUser = userRepository.save(user);
        
        createAndSendVerificationToken(savedUser);
        
        return userMapper.toRegisterResponse(savedUser);
    }
    
    private void validateEmailNotExists(String email) {
        if (!userRepository.existsByEmail(email)) {
            return;
        }
        
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email already exists: " + email));
        
        if (existingUser.isActive()) {
            throw new RuntimeException("Email already exists: " + email);
        }
        
        if (hasValidVerificationToken(existingUser)) {
            throw new RuntimeException("Email already exists: " + email);
        }
        
        deleteInactiveUser(existingUser);
        log.info("Deleted inactive user with expired/revoked tokens: {}", email);
    }
    
    private boolean hasValidVerificationToken(User user) {
        List<VerificationToken> tokens = verificationTokenRepository.findByUser(user);
        return tokens.stream()
                .anyMatch(token -> token.getExpiresAt().isAfter(Instant.now()));
    }
    
    private void deleteInactiveUser(User user) {
        verificationTokenRepository.deleteByUser(user);
        userRepository.delete(user);
    }
    
    private void validatePasswordMatch(RegisterRequest request) {
        if (request.getPassword() == null 
                || request.getConfirmPassword() == null 
                || !request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password and confirm password do not match");
        }
    }
    
    private User createUserFromRequest(RegisterRequest request) {
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(false);
        user.setRoles(getDefaultUserRole());
        return user;
    }
    
    private Set<Role> getDefaultUserRole() {
        Role userRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> new RuntimeException("Default role USER not found"));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        return roles;
    }
    
    private void createAndSendVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = createVerificationToken(user, token);
        verificationTokenRepository.save(verificationToken);
        sendVerificationEmail(user, token);
    }
    
    private VerificationToken createVerificationToken(User user, String token) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiresAt(Instant.now().plusSeconds(60L * 2L));
        return verificationToken;
    }
    
    private void sendVerificationEmail(User user, String token) {
        try {
            mailService.sendVerificationEmail(user.getEmail(), token, user.getId());
        } catch (Exception e) {
            log.warn("Failed to send verification email: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public AuthenticationResponse login(AuthenticationRequest request) {
        User user = findAndValidateUser(request.getEmail(), request.getPassword());
        
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenStr = createRefreshToken(user);
        
        AuthenticationResponse.UserInfo userInfo = userMapper.toUserInfo(user);
        
        return buildAuthenticationResponse(accessToken, refreshTokenStr, userInfo);
    }
    
    private User findAndValidateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        
        if (!user.isActive()) {
            throw new RuntimeException("Account not verified");
        }
        
        return user;
    }
    
    private String createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        
        String refreshTokenStr = UUID.randomUUID().toString();
        RefreshToken refreshToken = buildRefreshToken(user, refreshTokenStr);
        refreshTokenRepository.save(refreshToken);
        
        return refreshTokenStr;
    }
    
    private RefreshToken buildRefreshToken(User user, String token) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(60L * 60L * 24L * 30L));
        refreshToken.setRevoked(false);
        return refreshToken;
    }
    
    private AuthenticationResponse buildAuthenticationResponse(
            String accessToken, String refreshToken, AuthenticationResponse.UserInfo userInfo) {
        return AuthenticationResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .user(userInfo)
                .build();
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = validateRefreshToken(request.getRefreshToken());
        User user = storedToken.getUser();
        
        revokeRefreshToken(storedToken);
        String accessToken = jwtService.generateAccessToken(user);
        String newRefreshTokenStr = createNewRefreshToken(user);
        
        return buildRefreshTokenResponse(accessToken, newRefreshTokenStr);
    }
    
    private RefreshToken validateRefreshToken(String token) {
        RefreshToken stored = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired or revoked");
        }
        
        return stored;
    }
    
    private void revokeRefreshToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }
    
    private String createNewRefreshToken(User user) {
        String newRefreshTokenStr = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = buildRefreshToken(user, newRefreshTokenStr);
        refreshTokenRepository.save(newRefreshToken);
        return newRefreshTokenStr;
    }
    
    private RefreshTokenResponse buildRefreshTokenResponse(String accessToken, String refreshToken) {
        return RefreshTokenResponse.builder()
                .tokenType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
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
        User user = getCurrentAuthenticatedUser();
        validateOldPassword(user, request.getOldPassword());
        validateNewPassword(request.getNewPassword());
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    private User getCurrentAuthenticatedUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        if (email == null) {
            throw new RuntimeException("Not authenticated");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
    
    private void validateOldPassword(User user, String oldPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
    }
    
    private void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new RuntimeException("New password must not be empty");
        }
    }

    @Override
    public void createVerificationForUser(Long userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        VerificationToken verificationToken = createVerificationToken(user, token);
        verificationTokenRepository.save(verificationToken);
        sendVerificationEmail(user, token);
        
        log.info("Created verification token for email={} token={} userId={}",
                user.getEmail(), token, user.getId());
    }

    @Override
    public void sendPasswordResetEmail(ForgotPasswordRequest request) {
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
    }


    @Transactional
    @Override
    public void verifyEmailToken(String token) {
        VerificationToken verificationToken = findVerificationToken(token);
        validateTokenNotExpired(verificationToken);
        activateUser(verificationToken.getUser());
        verificationTokenRepository.deleteByToken(token);
    }

    @Transactional
    @Override
    public void verifyEmailTokenForUser(Long userId, String token) {
        VerificationToken verificationToken = findVerificationToken(token);
        validateTokenNotExpired(verificationToken);
        validateTokenBelongsToUser(verificationToken, userId);
        
        activateUser(verificationToken.getUser());
        verificationTokenRepository.deleteByToken(token);
    }
    
    private VerificationToken findVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));
    }
    
    private void validateTokenNotExpired(VerificationToken token) {
        if (token.getExpiresAt().isBefore(Instant.now())) {
            User expiredUser = token.getUser();
            verificationTokenRepository.delete(token);
            userRepository.delete(expiredUser);
            throw new RuntimeException(
                    "Verification token expired - account deleted. Please register again.");
        }
    }
    
    private void validateTokenBelongsToUser(VerificationToken token, Long userId) {
        if (!token.getUser().getId().equals(userId)) {
            throw new RuntimeException("Token does not belong to user");
        }
    }
    
    private void activateUser(User user) {
        user.setActive(true);
        userRepository.save(user);
    }


}
