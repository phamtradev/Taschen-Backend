package vn.edu.iuh.fit.bookstorebackend.user.controller;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.SetUserRoleCodesRequest;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.CreateUserRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.UpdateUserRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.response.UserResponse;
import vn.edu.iuh.fit.bookstorebackend.user.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() 
                || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String email = auth.getName();
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() 
                || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String email = auth.getName();
        UserResponse current = userService.getUserByEmail(email);

        if (request.getRoleCodes() != null) {
            if (!current.getRoles().contains("ADMIN")) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Only admin can update user roles");
            }
        } else {
            if (!current.getRoles().contains("ADMIN") 
                    && !current.getId().equals(id)) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Cannot update other user's profile");
            }
        }

        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<UserResponse> setActive(
            @PathVariable Long id,
            @RequestParam boolean active) {
        UserResponse user = userService.setActive(id, active);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PatchMapping("/{id}/roles/codes")
    public ResponseEntity<UserResponse> setRolesByCodes(
            @PathVariable Long id,
            @RequestBody SetUserRoleCodesRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() 
                || auth instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        String email = auth.getName();
        UserResponse currentUser = userService.getUserByEmail(email);

        if (!currentUser.getRoles().contains("ADMIN")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Only admin can change user roles");
        }

        UserResponse user = userService.setRolesByCodes(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
