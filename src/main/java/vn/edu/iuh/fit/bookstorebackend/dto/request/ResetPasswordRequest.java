package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    private String username;
    private String token;
    private String newPassword;
}
