package vn.edu.iuh.fit.bookstorebackend.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.auth.dto.request.AuthenticationRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.ChangePasswordRequest;
import vn.edu.iuh.fit.bookstorebackend.auth.dto.request.RefreshTokenRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.RegisterRequest;
import vn.edu.iuh.fit.bookstorebackend.auth.dto.request.VerifyTokenRequest;
import vn.edu.iuh.fit.bookstorebackend.auth.dto.response.AuthenticationResponse;
import vn.edu.iuh.fit.bookstorebackend.auth.dto.response.RefreshTokenResponse;
import vn.edu.iuh.fit.bookstorebackend.user.dto.response.RegisterResponse;
import vn.edu.iuh.fit.bookstorebackend.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyEmail(
            @RequestParam("token") String token,
            @RequestParam("userId") Long userId) {
        authService.verifyEmailTokenForUser(userId, token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify/{userId}")
    public ResponseEntity<Void> verifyEmailByUser(
            @PathVariable("userId") Long userId,
            @Valid @RequestBody VerifyTokenRequest request) {
        authService.verifyEmailTokenForUser(userId, request.getVerifyToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
