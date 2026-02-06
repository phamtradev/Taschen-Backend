package vn.edu.iuh.fit.bookstorebackend.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@Data
@JsonPropertyOrder({ "id", "bookId", "bookTitle", "quantity", "importPrice" })
public class PurchaseOrderItemResponse {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private int quantity;
    private double importPrice;
}
