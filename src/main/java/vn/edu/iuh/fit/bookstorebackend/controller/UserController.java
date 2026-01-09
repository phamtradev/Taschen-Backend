package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateUserRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.SetUserRolesRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateUserRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.UserResponse;
import vn.edu.iuh.fit.bookstorebackend.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
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

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        UserResponse user = userService.updateUser(id, request);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<UserResponse> setActive(@PathVariable Long id, @RequestParam boolean active) {
        UserResponse user = userService.setActive(id, active);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PatchMapping("/{id}/roles")
    public ResponseEntity<UserResponse> setRoles(@PathVariable Long userId, @RequestBody SetUserRolesRequest request) {
        UserResponse user = userService.setRoles(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
