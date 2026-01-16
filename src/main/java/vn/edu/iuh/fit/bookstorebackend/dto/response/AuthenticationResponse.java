package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"userId", "email", "expiresIn", "tokenType", "refreshToken", "accessToken", "roles"})
public class AuthenticationResponse {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String email;
    private long expiresIn;
    private List<String> roles;
}
