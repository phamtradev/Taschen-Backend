package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "variantFormat", "bookId", "bookTitle" })
public class VariantResponse {
    private Long id;
    private VariantFormatDTO variantFormat;
    private Long bookId;
    private String bookTitle;
    
    @Data
    @JsonPropertyOrder({ "id", "code", "name" })
    public static class VariantFormatDTO {
        private Long id;
        private String code;
        private String name;
    }
}
