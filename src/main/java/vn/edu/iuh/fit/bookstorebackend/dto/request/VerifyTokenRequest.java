package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyTokenRequest {
    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Verify token is required")
    private String verifyToken;
}