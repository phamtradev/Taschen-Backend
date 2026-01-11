package vn.edu.iuh.fit.bookstorebackend.dto.request;

import lombok.Getter;
import lombok.Setter;
import vn.edu.iuh.fit.bookstorebackend.common.AddressType;

@Getter
@Setter
public class AddressRequest {
    private AddressType addressType;
    private String street;
    private String district;
    private String ward;
    private String city;
    private String recipientName;
    private String phoneNumber;
    private Boolean isDefault;
}


