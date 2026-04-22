package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateVariantRequest {
    @NotBlank(message = "Format code is required")
    @Size(max = 50, message = "Format code must not exceed 50 characters")
    private String formatCode;

    @NotBlank(message = "Format name is required")
    @Size(max = 100, message = "Format name must not exceed 100 characters")
    private String formatName;

    @Positive(message = "Book ID must be positive")
    private Long bookId;
}
