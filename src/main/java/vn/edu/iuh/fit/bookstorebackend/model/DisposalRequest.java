package vn.edu.iuh.fit.bookstorebackend.model;

import jakarta.persistence.*;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.DisposalRequestStatus;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "disposal_requests")
public class DisposalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reason", length = 1000)
    private String reason;

    @Column(name = "response_note", length = 1000)
    private String responseNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DisposalRequestStatus status = DisposalRequestStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy; // warehouse staff

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_user_id")
    private User processedBy; // admin

    @OneToMany(mappedBy = "disposalRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DisposalRequestItem> items;
}
