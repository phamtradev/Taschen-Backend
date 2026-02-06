package vn.edu.iuh.fit.bookstorebackend.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.StockRequestStatus;

@Entity
@Data
@Table(name = "stock_requests")
public class StockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StockRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "response_message", length = 1000)
    private String responseMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_user_id")
    private User processedBy;

}