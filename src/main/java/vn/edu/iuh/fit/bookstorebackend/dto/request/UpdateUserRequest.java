package vn.edu.iuh.fit.bookstorebackend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.iuh.fit.bookstorebackend.common.Gender;

import java.util.List;

@Getter
@Setter
public class UpdateUserRequest {
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    private Gender gender;

    @Pattern(regexp = "^0[0-9]{9}$", message = "Phone number must be 10 digits starting with 0")
    private String phoneNumber;

    private Boolean active;

    private List<String> roleCodes;
}
