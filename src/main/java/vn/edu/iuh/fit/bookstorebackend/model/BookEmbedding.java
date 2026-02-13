package vn.edu.iuh.fit.bookstorebackend.model;

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

    @Column(name = "vector", columnDefinition = "JSON")
    private String vector; // Stored as JSON array: "[0.1, 0.2, 0.3, ...]"

    @Column(name = "model")
    private String model; // e.g., "tfidf", "openai", "sentence-transformer"

    @Column(name = "dimension")
    private Integer dimension; // e.g., 384, 1536

    @Column(name = "text_used", columnDefinition = "TEXT")
    private String textUsed; // The text used to generate embedding (title + description)
}
