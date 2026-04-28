package vn.edu.iuh.fit.bookstorebackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.bookstorebackend.dto.request.BannerRequest;
import vn.edu.iuh.fit.bookstorebackend.dto.response.BannerResponse;
import vn.edu.iuh.fit.bookstorebackend.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.service.BannerService;
import vn.edu.iuh.fit.bookstorebackend.service.CloudinaryService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banners")
public class BannerController {

    private final BannerService bannerService;
    private final CloudinaryService cloudinaryService;

    public BannerController(BannerService bannerService, CloudinaryService cloudinaryService) {
        this.bannerService = bannerService;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> result = cloudinaryService.uploadImage(file, "banners");
        return ResponseEntity.ok(result);
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
