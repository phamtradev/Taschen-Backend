package vn.edu.iuh.fit.bookstorebackend.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private long expiresIn;
}
