package vn.edu.iuh.fit.bookstorebackend.user.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.iuh.fit.bookstorebackend.shared.common.Gender;

import java.util.Set;

@Getter
@Setter
public class CreateUserRequest {
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Phone number must be 10 digits starting with 0")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    private Boolean active;

    private Set<String> roleCodes;
}
