package vn.edu.iuh.fit.bookstorebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.common.PromotionStatus;
import vn.edu.iuh.fit.bookstorebackend.dto.request.CreatePromotionRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.PromotionResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.PromotionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {

    private final PromotionService promotionService;

    @PostMapping
    public ResponseEntity<PromotionResponse> createPromotion(@RequestBody CreatePromotionRequest request) throws IdInvalidException {
        PromotionResponse promotionResponse = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(promotionResponse);
    }

    @GetMapping("/search")
    public ResponseEntity<List<PromotionResponse>> searchPromotions(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) PromotionStatus status,
            @RequestParam(required = false) Boolean isActive) {
        List<PromotionResponse> promotions = promotionService.searchPromotions(name, code, status, isActive);
        return ResponseEntity.status(HttpStatus.OK).body(promotions);
    }

    @GetMapping("/validate/{code}")
    public ResponseEntity<Map<String, Boolean>> validatePromotionCode(@PathVariable String code) {
        boolean isValid = promotionService.validatePromotionCode(code);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isValid", isValid);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getAllPromotions() {
        List<PromotionResponse> promotions = promotionService.getAllPromotions();
        return ResponseEntity.status(HttpStatus.OK).body(promotions);
    }

    @GetMapping("/{promotionId}")
    public ResponseEntity<PromotionResponse> getPromotionById(@PathVariable Long promotionId) throws IdInvalidException {
        PromotionResponse promotionResponse = promotionService.getPromotionById(promotionId);
        return ResponseEntity.status(HttpStatus.OK).body(promotionResponse);
    }

    @PatchMapping("/{promotionId}/approve")
    public ResponseEntity<PromotionResponse> approvePromotion(@PathVariable Long promotionId) throws IdInvalidException {
        PromotionResponse promotionResponse = promotionService.approvePromotion(promotionId);
        return ResponseEntity.status(HttpStatus.OK).body(promotionResponse);
    }

    @PatchMapping("/{promotionId}/deactivate")
    public ResponseEntity<PromotionResponse> deactivatePromotion(@PathVariable Long promotionId) throws IdInvalidException {
        PromotionResponse promotionResponse = promotionService.deactivatePromotion(promotionId);
        return ResponseEntity.status(HttpStatus.OK).body(promotionResponse);
    }

    @PatchMapping("/{promotionId}/pause")
    public ResponseEntity<PromotionResponse> pausePromotion(@PathVariable Long promotionId) throws IdInvalidException {
        PromotionResponse promotionResponse = promotionService.pausePromotion(promotionId);
        return ResponseEntity.status(HttpStatus.OK).body(promotionResponse);
    }

    @PatchMapping("/{promotionId}/resume")
    public ResponseEntity<PromotionResponse> resumePromotion(@PathVariable Long promotionId) throws IdInvalidException {
        PromotionResponse promotionResponse = promotionService.resumePromotion(promotionId);
        return ResponseEntity.status(HttpStatus.OK).body(promotionResponse);
    }

    @PostMapping("/{promotionId}/notify-paused")
    public ResponseEntity<Map<String, String>> notifyPromotionPaused(@PathVariable Long promotionId) throws IdInvalidException {
        promotionService.notifyPromotionPaused(promotionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification sent successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{promotionId}/notify-customers")
    public ResponseEntity<Map<String, String>> notifyActiveCustomers(@PathVariable Long promotionId) throws IdInvalidException {
        promotionService.notifyActiveCustomers(promotionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification sent successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{promotionId}/notify-resumed")
    public ResponseEntity<Map<String, String>> notifyPromotionResumed(@PathVariable Long promotionId) throws IdInvalidException {
        promotionService.notifyPromotionResumed(promotionId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Notification sent successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
