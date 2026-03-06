package vn.edu.iuh.fit.bookstorebackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "disposal_request_items")
public class DisposalRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "remaining_quantity_after")
    private Integer remainingQuantityAfter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disposal_request_id", nullable = false)
    private DisposalRequest disposalRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;
}
