package vn.edu.iuh.fit.bookstorebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.AddressRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.AddressResponse;
import vn.edu.iuh.fit.bookstorebackend.service.AddressService;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getByUser(
            @PathVariable Long userId) throws IdInvalidException {

        return ResponseEntity.ok(addressService.getAddressesByUserId(userId));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @PathVariable Long userId,
            @RequestBody AddressRequest request) throws IdInvalidException {

        AddressResponse resp = addressService.createAddress(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getById(
            @PathVariable Long userId,
            @PathVariable("id") Long addressId) throws IdInvalidException {

        return ResponseEntity.ok(addressService.getAddressById(addressId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(
            @PathVariable Long userId,
            @PathVariable("id") Long addressId,
            @RequestBody AddressRequest request) throws IdInvalidException {

        return ResponseEntity.ok(addressService.updateAddress(addressId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long userId,
            @PathVariable("id") Long addressId) throws IdInvalidException {

        addressService.deleteAddress(addressId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<Void> setDefault(
            @PathVariable Long userId,
            @PathVariable("id") Long addressId) throws IdInvalidException {

        addressService.setDefaultAddress(userId, addressId);
        return ResponseEntity.noContent().build();
    }
}