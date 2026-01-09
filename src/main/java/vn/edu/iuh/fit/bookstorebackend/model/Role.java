package vn.edu.iuh.fit.bookstorebackend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 100)
    private String code; //GUEST, USER, ADMIN, SELLER, WAREHOUSE_STAFF

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private List<Permission> permissions;
}
