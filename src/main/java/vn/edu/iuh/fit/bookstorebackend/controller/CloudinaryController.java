package vn.edu.iuh.fit.bookstorebackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.bookstorebackend.service.CloudinaryService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/cloudinary")
@RequiredArgsConstructor
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) 
            throws IOException {
        return ResponseEntity.ok(cloudinaryService.uploadImage(file));
    }

    @PostMapping("/upload/{folder}")
    public ResponseEntity<Map<String, Object>> uploadImageToFolder(
            @RequestParam("file") MultipartFile file,
            @PathVariable String folder) throws IOException {
        return ResponseEntity.ok(cloudinaryService.uploadImage(file, folder));
    }

    @DeleteMapping("/delete/{*publicId}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable("publicId") String publicId)
            throws IOException {
        return ResponseEntity.ok(cloudinaryService.deleteImage(publicId));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteImageByQuery(@RequestParam("publicId") String publicId)
            throws IOException {
        return ResponseEntity.ok(cloudinaryService.deleteImage(publicId));
    }

}
