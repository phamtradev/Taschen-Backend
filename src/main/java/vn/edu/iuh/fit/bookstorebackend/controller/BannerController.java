package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.bookstorebackend.dto.request.BannerRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BannerResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.BannerService;

import java.util.List;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    @PostMapping
    public ResponseEntity<BannerResponse> createBanner(
            @RequestBody BannerRequest request) throws IdInvalidException {
        BannerResponse bannerResponse = bannerService.createBanner(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(bannerResponse);
    }

    @GetMapping
    public ResponseEntity<List<BannerResponse>> getAllBanners() {
        List<BannerResponse> banners = bannerService.getAllBanners();
        return ResponseEntity.status(HttpStatus.OK).body(banners);
    }

    @GetMapping("/{bannerId}")
    public ResponseEntity<BannerResponse> getBannerById(
            @PathVariable Long bannerId) throws IdInvalidException {
        BannerResponse bannerResponse = bannerService.getBannerById(bannerId);
        return ResponseEntity.status(HttpStatus.OK).body(bannerResponse);
    }

    @PutMapping("/{bannerId}")
    public ResponseEntity<BannerResponse> updateBanner(
            @PathVariable Long bannerId,
            @RequestBody BannerRequest request) throws IdInvalidException {
        BannerResponse bannerResponse = bannerService.updateBanner(bannerId, request);
        return ResponseEntity.status(HttpStatus.OK).body(bannerResponse);
    }

    @DeleteMapping("/{bannerId}")
    public ResponseEntity<Void> deleteBanner(
            @PathVariable Long bannerId) throws IdInvalidException {
        bannerService.deleteBanner(bannerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
