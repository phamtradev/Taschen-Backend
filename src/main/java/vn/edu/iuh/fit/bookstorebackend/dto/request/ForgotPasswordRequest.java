package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {
    private String username;
    private String email;
}
