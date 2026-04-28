package vn.edu.iuh.fit.bookstorebackend.auth.service;
import vn.edu.iuh.fit.bookstorebackend.auth.dto.request.AuthenticationRequest;
import vn.edu.iuh.fit.bookstorebackend.auth.dto.request.ForgotPasswordRequest;
import vn.edu.iuh.fit.bookstorebackend.auth.dto.request.RefreshTokenRequest;
import vn.edu.iuh.fit.bookstorebackend.auth.dto.request.ResetPasswordRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.ChangePasswordRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.RegisterRequest;

import vn.edu.iuh.fit.bookstorebackend.auth.dto.response.AuthenticationResponse;
import vn.edu.iuh.fit.bookstorebackend.auth.dto.response.RefreshTokenResponse;
import vn.edu.iuh.fit.bookstorebackend.user.dto.response.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

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
