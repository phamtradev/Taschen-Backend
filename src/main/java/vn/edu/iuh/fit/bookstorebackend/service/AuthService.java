package vn.edu.iuh.fit.bookstorebackend.service;

import vn.edu.iuh.fit.bookstorebackend.dto.request.*;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AuthenticationResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.RefreshTokenResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.UserResponse;

public interface AuthService {

    vn.edu.iuh.fit.bookstorebackend.dto.response.RegisterResponse register(RegisterRequest request);

    AuthenticationResponse login(AuthenticationRequest request);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

    void changePassword(ChangePasswordRequest request);

    void sendPasswordResetEmail(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void createVerificationForUser(Long userId, String token);

    void verifyEmailToken(String token);
    
    void verifyEmailTokenForUser(Long userId, String token);
}
