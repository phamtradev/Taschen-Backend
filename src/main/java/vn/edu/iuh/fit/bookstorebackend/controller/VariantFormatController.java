package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreateVariantFormatRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.request.UpdateVariantFormatRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.VariantFormatResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.VariantFormatService;

import java.util.List;

@RestController
@RequestMapping("/api/variant-formats")
public class VariantFormatController {

    private final VariantFormatService variantFormatService;

    public VariantFormatController(VariantFormatService variantFormatService) {
        this.variantFormatService = variantFormatService;
    }

    @PostMapping
    public ResponseEntity<VariantFormatResponse> createVariantFormat(
            @RequestBody CreateVariantFormatRequest request) throws IdInvalidException {
        VariantFormatResponse variantFormatResponse = variantFormatService.createVariantFormat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(variantFormatResponse);
    }

    @GetMapping
    public ResponseEntity<List<VariantFormatResponse>> getAllVariantFormats() {
        List<VariantFormatResponse> variantFormats = variantFormatService.getAllVariantFormats();
        return ResponseEntity.status(HttpStatus.OK).body(variantFormats);
    }

    @GetMapping("/{variantFormatId}")
    public ResponseEntity<VariantFormatResponse> getVariantFormatById(
            @PathVariable Long variantFormatId) throws IdInvalidException {
        VariantFormatResponse variantFormatResponse = variantFormatService.getVariantFormatById(variantFormatId);
        return ResponseEntity.status(HttpStatus.OK).body(variantFormatResponse);
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<VariantFormatResponse> getVariantFormatByCode(
            @PathVariable String code) throws IdInvalidException {
        VariantFormatResponse variantFormatResponse = variantFormatService.getVariantFormatByCode(code);
        return ResponseEntity.status(HttpStatus.OK).body(variantFormatResponse);
    }

    @PutMapping("/{variantFormatId}")
    public ResponseEntity<VariantFormatResponse> updateVariantFormat(
            @PathVariable Long variantFormatId,
            @RequestBody UpdateVariantFormatRequest request) throws IdInvalidException {
        VariantFormatResponse variantFormatResponse = variantFormatService.updateVariantFormat(
                variantFormatId, request);
        return ResponseEntity.status(HttpStatus.OK).body(variantFormatResponse);
    }

    @DeleteMapping("/{variantFormatId}")
    public ResponseEntity<Void> deleteVariantFormat(
            @PathVariable Long variantFormatId) throws IdInvalidException {
        variantFormatService.deleteVariantFormat(variantFormatId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
