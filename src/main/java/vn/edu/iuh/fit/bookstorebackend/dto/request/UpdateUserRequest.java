package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;
import vn.edu.iuh.fit.bookstorebackend.common.Gender;

@Getter
@Setter
public class UpdateUserRequest {
    private String firstName;
    private String lastName;

    private Gender gender;

    private String phoneNumber;

    private Boolean active;
}
