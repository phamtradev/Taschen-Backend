package vn.edu.iuh.fit.bookstorebackend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponse {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
