package vn.edu.iuh.fit.bookstorebackend.model;

import jakarta.persistence.*;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.common.PromotionStatus;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Table(name = "promotions")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "discount_percent", nullable = false)
    private double discountPercent;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private PromotionStatus status; // PENDING, ACTIVE, REJECTED, PAUSED

    @Column(name = "price_order_active")
    private double priceOrderActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy; //staff

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_user_id")
    private User approvedBy; //staff

    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY)
    private List<Order> orders;
}
