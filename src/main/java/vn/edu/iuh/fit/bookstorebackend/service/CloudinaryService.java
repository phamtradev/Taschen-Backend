package vn.edu.iuh.fit.bookstorebackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {

    Map<String, Object> uploadImage(MultipartFile file) throws IOException;

    Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException;

    Map<String, Object> deleteImage(String publicId) throws IOException;
}
