package vn.edu.iuh.fit.bookstorebackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "banners")
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;
}
