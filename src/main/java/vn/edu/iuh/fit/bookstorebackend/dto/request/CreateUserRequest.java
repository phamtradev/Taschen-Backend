package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;
import vn.edu.iuh.fit.bookstorebackend.common.Gender;

import java.util.Set;

@Getter
@Setter
public class CreateUserRequest {
    private String firstName;
    private String lastName;

    private Gender gender;

    private String email;
    private String phoneNumber;
    
    private String password;

    private Boolean active;

    private Set<String> roleCodes;
}
