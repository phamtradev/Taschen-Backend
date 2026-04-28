package vn.edu.iuh.fit.bookstorebackend.marketing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.bookstorebackend.marketing.dto.request.BannerRequest;
import vn.edu.iuh.fit.bookstorebackend.marketing.dto.response.BannerResponse;
import vn.edu.iuh.fit.bookstorebackend.shared.exception.IdInvalidException;
import vn.edu.iuh.fit.bookstorebackend.shared.model.RestRespone;
import vn.edu.iuh.fit.bookstorebackend.marketing.service.BannerService;
import vn.edu.iuh.fit.bookstorebackend.shared.service.CloudinaryService;

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
    public ResponseEntity<RestRespone<BannerResponse>> createBanner(
            @RequestBody BannerRequest request) throws IdInvalidException {
        BannerResponse bannerResponse = bannerService.createBanner(request);
        RestRespone<BannerResponse> response = new RestRespone<>();
        response.setStatusCode(201);
        response.setMessage("Tạo banner thành công");
        response.setData(bannerResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<RestRespone<List<BannerResponse>>> getAllBanners() {
        List<BannerResponse> banners = bannerService.getAllBanners();
        RestRespone<List<BannerResponse>> response = new RestRespone<>();
        response.setStatusCode(200);
        response.setMessage("Success");
        response.setData(banners);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bannerId}")
    public ResponseEntity<RestRespone<BannerResponse>> getBannerById(
            @PathVariable Long bannerId) throws IdInvalidException {
        BannerResponse bannerResponse = bannerService.getBannerById(bannerId);
        RestRespone<BannerResponse> response = new RestRespone<>();
        response.setStatusCode(200);
        response.setMessage("Success");
        response.setData(bannerResponse);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{bannerId}")
    public ResponseEntity<RestRespone<BannerResponse>> updateBanner(
            @PathVariable Long bannerId,
            @RequestBody BannerRequest request) throws IdInvalidException {
        BannerResponse bannerResponse = bannerService.updateBanner(bannerId, request);
        RestRespone<BannerResponse> response = new RestRespone<>();
        response.setStatusCode(200);
        response.setMessage("Cập nhật banner thành công");
        response.setData(bannerResponse);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{bannerId}")
    public ResponseEntity<RestRespone<Void>> deleteBanner(
            @PathVariable Long bannerId) throws IdInvalidException {
        bannerService.deleteBanner(bannerId);
        RestRespone<Void> response = new RestRespone<>();
        response.setStatusCode(204);
        response.setMessage("Xóa banner thành công");
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
