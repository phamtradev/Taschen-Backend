package vn.edu.iuh.fit.bookstorebackend.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WsEvent {
    private String type;   // CREATED | UPDATED | DELETED
    private String entity; // tên entity
    private Object id;     // id của record
    private Object data;   // optional, null nếu không cần
}
