package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyTokenRequest {
    private Long userId;
    private String verifyToken;
}


