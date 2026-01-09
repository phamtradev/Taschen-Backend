package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookstorebackend.dto.request.*;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AuthenticationResponse;
import vn.edu.iuh.fit.bookstorebackend.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {
    @Override
    public void register(RegisterRequest request) {

    }

    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {
        return null;
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        return null;
    }

    @Override
    public void logout(String refreshToken) {

    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

    }

    @Override
    public void sendPasswordResetEmail(ForgotPasswordRequest request) {

    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {

    }
}
