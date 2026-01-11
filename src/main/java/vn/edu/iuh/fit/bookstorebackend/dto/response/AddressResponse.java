package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.AddressType;

@Data
@JsonPropertyOrder({ "id", "addressType", "street", "district", "ward", "city", "recipientName", "phoneNumber", "isDefault" })
public class AddressResponse {
    private Long id;
    private AddressType addressType;
    private String street;
    private String district;
    private String ward;
    private String city;
    private String recipientName;
    private String phoneNumber;
    private Boolean isDefault;
}
