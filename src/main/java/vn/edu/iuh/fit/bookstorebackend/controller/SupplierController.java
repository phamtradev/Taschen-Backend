package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateSupplierRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateSupplierRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.SupplierResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.SupplierService;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers() {
        List<SupplierResponse> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.status(HttpStatus.OK).body(suppliers);
    }

    @GetMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> getSupplierById(
            @PathVariable Long supplierId) throws IdInvalidException {
        SupplierResponse supplierResponse = supplierService.getSupplierById(supplierId);
        return ResponseEntity.status(HttpStatus.OK).body(supplierResponse);
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(
            @RequestBody CreateSupplierRequest request) throws IdInvalidException {
        SupplierResponse supplierResponse = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierResponse);
    }

    @PutMapping("/{supplierId}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long supplierId,
            @RequestBody UpdateSupplierRequest request) throws IdInvalidException {
        SupplierResponse supplierResponse = supplierService.updateSupplier(supplierId, request);
        return ResponseEntity.status(HttpStatus.OK).body(supplierResponse);
    }

    @DeleteMapping("/{supplierId}")
    public ResponseEntity<Void> deleteSupplier(
            @PathVariable Long supplierId) throws IdInvalidException {
        supplierService.deleteSupplier(supplierId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
