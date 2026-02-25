package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "formatCode", "formatName" })
public class VariantResponse {
    private Long id;
    private String formatCode;
    private String formatName;
}
