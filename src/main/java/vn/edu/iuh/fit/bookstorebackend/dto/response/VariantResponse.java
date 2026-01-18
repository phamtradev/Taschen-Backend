package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "format", "bookId", "bookTitle" })
public class VariantResponse {
    private Long id;
    private String format;
    private Long bookId;
    private String bookTitle;
}
