package vn.edu.iuh.fit.bookstorebackend.book.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "book_embeddings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false, unique = true)
    private Long bookId;

    @Lob
    @Column(name = "vector", columnDefinition = "MEDIUMTEXT")
    private String vector;

    @Column(name = "model")
    private String model;

    @Column(name = "dimension")
    private Integer dimension;

    @Column(name = "text_used", columnDefinition = "TEXT")
    private String textUsed;
}
