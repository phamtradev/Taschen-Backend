package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateVariantRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.VariantResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.VariantService;

import java.util.List;

@RestController
@RequestMapping("/api/variants")
public class VariantController {

    private final VariantService variantService;

    public VariantController(VariantService variantService) {
        this.variantService = variantService;
    }

    @PostMapping
    public ResponseEntity<VariantResponse> createVariant(
            @RequestBody CreateVariantRequest request) throws IdInvalidException {
        VariantResponse variantResponse = variantService.createVariant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(variantResponse);
    }

    @GetMapping
    public ResponseEntity<List<VariantResponse>> getAllVariants() {
        List<VariantResponse> variants = variantService.getAllVariants();
        return ResponseEntity.status(HttpStatus.OK).body(variants);
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<VariantResponse>> getVariantsByBookId(
            @PathVariable Long bookId) throws IdInvalidException {
        List<VariantResponse> variants = variantService.getVariantsByBookId(bookId);
        return ResponseEntity.status(HttpStatus.OK).body(variants);
    }

    @GetMapping("/{variantId}")
    public ResponseEntity<VariantResponse> getVariantById(
            @PathVariable Long variantId) throws IdInvalidException {
        VariantResponse variantResponse = variantService.getVariantById(variantId);
        return ResponseEntity.status(HttpStatus.OK).body(variantResponse);
    }

    @PutMapping("/{variantId}")
    public ResponseEntity<VariantResponse> updateVariant(
            @PathVariable Long variantId,
            @RequestBody UpdateVariantRequest request) throws IdInvalidException {
        VariantResponse variantResponse = variantService.updateVariant(
                variantId, request);
        return ResponseEntity.status(HttpStatus.OK).body(variantResponse);
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<Void> deleteVariant(
            @PathVariable Long variantId) throws IdInvalidException {
        variantService.deleteVariant(variantId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
