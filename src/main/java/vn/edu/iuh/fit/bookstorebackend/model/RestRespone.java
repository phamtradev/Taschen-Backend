package vn.edu.iuh.fit.bookstorebackend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({ "error", "message", "statusCode", "data" })
public class RestRespone<T> {
    private String error;
    private Object message; //co the string hoac arrayList
    private int statusCode;
    private T data;
}
