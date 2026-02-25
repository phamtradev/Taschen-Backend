package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "code", "name" })
public class VariantFormatResponse {
    private Long id;
    private String code;
    private String name;
}
