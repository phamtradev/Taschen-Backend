package vn.edu.iuh.fit.bookstorebackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    
    /**
     * Upload hình ảnh lên Cloudinary
     * @param file File hình ảnh cần upload
     * @return Map chứa thông tin về hình ảnh đã upload (url, public_id, etc.)
     * @throws IOException Nếu có lỗi khi đọc file
     */
    Map<String, Object> uploadImage(MultipartFile file) throws IOException;
    
    /**
     * Upload hình ảnh lên Cloudinary với folder cụ thể
     * @param file File hình ảnh cần upload
     * @param folder Folder trên Cloudinary để lưu hình ảnh
     * @return Map chứa thông tin về hình ảnh đã upload
     * @throws IOException Nếu có lỗi khi đọc file
     */
    Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException;
    
    /**
     * Xóa hình ảnh từ Cloudinary bằng public_id
     * @param publicId Public ID của hình ảnh trên Cloudinary
     * @return Map chứa kết quả xóa
     * @throws IOException Nếu có lỗi khi xóa
     */
    Map<String, Object> deleteImage(String publicId) throws IOException;
}
