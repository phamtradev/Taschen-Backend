package vn.edu.iuh.fit.bookstorebackend.marketing.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "name", "imageUrl" })
public class BannerResponse {
    private Long id;
    private String name;
    private String imageUrl;
}
