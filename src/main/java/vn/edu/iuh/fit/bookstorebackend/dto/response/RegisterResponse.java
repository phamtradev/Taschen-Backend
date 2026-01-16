package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.Gender;

import java.util.List;

@Data
@JsonPropertyOrder({ "id", "email", "firstName", "lastName", "gender", "phoneNumber", "active", "roles", "addresses" })
public class RegisterResponse {
    private Long id;
    private String email;

    private String firstName;
    private String lastName;
    private Gender gender;
    private String phoneNumber;

    private boolean isActive;

    private List<String> roles;
    private List<AddressResponse> addresses;
}


