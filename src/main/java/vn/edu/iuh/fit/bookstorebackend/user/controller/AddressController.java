package vn.edu.iuh.fit.bookstorebackend.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.iuh.fit.bookstorebackend.user.dto.request.AddressRequest;
import vn.edu.iuh.fit.bookstorebackend.user.dto.response.AddressResponse;
import vn.edu.iuh.fit.bookstorebackend.user.service.AddressService;
import vn.edu.iuh.fit.bookstorebackend.user.service.UserService;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final UserService userService;

    private Long resolveUserId(String userId) {
        if ("me".equalsIgnoreCase(userId)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
            }
            return userService.getUserByEmail(auth.getName()).getId();
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid userId: " + userId);
        }
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getByUser(
            @PathVariable String userId) throws IdInvalidException {

        return ResponseEntity.ok(addressService.getAddressesByUserId(resolveUserId(userId)));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @PathVariable String userId,
            @Valid @RequestBody AddressRequest request) throws IdInvalidException {

        AddressResponse resp = addressService.createAddress(resolveUserId(userId), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getById(
            @PathVariable String userId,
            @PathVariable("id") Long addressId) throws IdInvalidException {

        return ResponseEntity.ok(addressService.getAddressById(addressId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(
            @PathVariable String userId,
            @PathVariable("id") Long addressId,
            @Valid @RequestBody AddressRequest request) throws IdInvalidException {

        return ResponseEntity.ok(addressService.updateAddress(addressId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String userId,
            @PathVariable("id") Long addressId) throws IdInvalidException {

        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<Void> setDefault(
            @PathVariable String userId,
            @PathVariable("id") Long addressId) throws IdInvalidException {

        addressService.setDefaultAddress(resolveUserId(userId), addressId);
        return ResponseEntity.noContent().build();
    }
}