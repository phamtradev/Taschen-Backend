package vn.edu.iuh.fit.bookstorebackend.service.Impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.bookstorebackend.service.CloudinaryService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.default-folder:bookstore}")
    private String defaultFolder;

    @Override
    public Map<String, Object> uploadImage(MultipartFile file) throws IOException {
        return uploadImage(file, defaultFolder);
    }

    @Override
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File phải là hình ảnh");
        }

        // Validate file size (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 10MB");
        }

        try {
            // Tạo public_id duy nhất (bao gồm folder trong public_id)
            String publicId = folder + "/" + UUID.randomUUID().toString();

            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("public_id", publicId);
            uploadParams.put("overwrite", false);
            // Không set "folder" param vì đã có trong public_id, nếu set sẽ bị duplicate folder

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader()
                    .upload(file.getBytes(), uploadParams);

            return uploadResult;
        } catch (IOException e) {
            throw new IOException("Lỗi khi upload hình ảnh lên Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Public ID không được để trống");
        }

        try {
            publicId = publicId.trim();
            @SuppressWarnings("unchecked")
            Map<String, Object> deleteParams = (Map<String, Object>) ObjectUtils.asMap(
                    "invalidate", true,
                    // explicit để tránh trường hợp Cloudinary hiểu sai type/resource
                    "resource_type", "image",
                    "type", "upload"
            );
            @SuppressWarnings("unchecked")
            Map<String, Object> deleteResult = (Map<String, Object>) cloudinary.uploader()
                    .destroy(publicId, deleteParams);

            return deleteResult;
        } catch (IOException e) {
            throw new IOException("Lỗi khi xóa hình ảnh từ Cloudinary: " + e.getMessage(), e);
        }
    }

}
