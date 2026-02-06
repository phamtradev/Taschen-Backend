package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSupplierRequest {

    private String name;
    private String email;
    private String phone;
    private String address;
}

