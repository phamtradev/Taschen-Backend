package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.AuthenticationRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.ChangePasswordRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.RefreshTokenRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.RegisterRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.VerifyTokenRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AuthenticationResponse;
import vn.edu.iuh.fit.bookstorebackend.dto.response.RefreshTokenResponse;
import vn.edu.iuh.fit.bookstorebackend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<vn.edu.iuh.fit.bookstorebackend.dto.response.RegisterResponse> register(@RequestBody RegisterRequest request) {
        vn.edu.iuh.fit.bookstorebackend.dto.response.RegisterResponse user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token, @RequestParam("userId") Long userId) {
        authService.verifyEmailTokenForUser(userId, token);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify/{userId}")
    public ResponseEntity<Void> verifyEmailByUser(@PathVariable("userId") Long userId, @RequestBody VerifyTokenRequest request) {
        authService.verifyEmailTokenForUser(userId, request.getVerifyToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
