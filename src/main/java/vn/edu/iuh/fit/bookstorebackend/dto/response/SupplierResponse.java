package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "name", "email", "phone", "address", "isActive" })
public class SupplierResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private boolean isActive;
}

