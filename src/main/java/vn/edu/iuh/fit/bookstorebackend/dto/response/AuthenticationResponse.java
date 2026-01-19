package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import vn.edu.iuh.fit.bookstorebackend.common.Gender;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"user", "tokenType", "accessToken", "refreshToken", "expiresIn"})
public class AuthenticationResponse {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserInfo user;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonPropertyOrder({"id", "email", "firstName", "lastName", "gender", "phoneNumber", "roles"})
    public static class UserInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private Gender gender;
        private String phoneNumber;
        private List<String> roles;
    }
}
