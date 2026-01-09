package vn.edu.iuh.fit.bookstorebackend.model;

import jakarta.persistence.*;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.HttpMethod;

@Entity
@Data
@Table(name = "perrmissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "http_method", nullable = false, length = 10)
    private HttpMethod httpMethod;

    @Column(name = "path_pattern", nullable = false, length = 255)
    private String pathPattern;    //api/books/**

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
