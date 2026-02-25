package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.List;

@Data
@JsonPropertyOrder({ "id", "formatCode", "formatName", "bookIds" })
public class VariantResponse {
    private Long id;
    private String formatCode;
    private String formatName;
    private List<Long> bookIds;
}
