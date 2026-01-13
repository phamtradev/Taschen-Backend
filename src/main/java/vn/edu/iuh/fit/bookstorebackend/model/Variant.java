package vn.edu.iuh.fit.bookstorebackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "variants")
public class Variant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String format;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
}
