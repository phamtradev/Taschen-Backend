package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"userId", "username", "expiresIn", "tokenType", "refreshToken", "accessToken"})
public class AuthenticationResponse {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private long expiresIn;
}
