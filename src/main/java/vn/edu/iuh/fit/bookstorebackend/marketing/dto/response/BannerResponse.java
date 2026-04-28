package vn.edu.iuh.fit.bookstorebackend.marketing.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "name", "subtitle", "tag", "imageUrl" })
public class BannerResponse {
    private Long id;
    private String name;
    private String subtitle;
    private String tag;
    private String imageUrl;
}
