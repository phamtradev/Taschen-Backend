package vn.edu.iuh.fit.bookstorebackend.service;


import vn.edu.iuh.fit.bookstorebackend.dto.request.*;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AuthenticationResponse;

public interface AuthService {

    void register(RegisterRequest request);

    AuthenticationResponse login(AuthenticationRequest request);

    AuthenticationResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

    void changePassword(ChangePasswordRequest request);

    void sendPasswordResetEmail(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
