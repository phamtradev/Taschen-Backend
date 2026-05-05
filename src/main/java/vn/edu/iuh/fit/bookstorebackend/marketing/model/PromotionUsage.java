package vn.edu.iuh.fit.bookstorebackend.marketing.model;

import jakarta.persistence.*;
import lombok.Data;
import vn.edu.iuh.fit.bookstorebackend.order.model.Order;
import vn.edu.iuh.fit.bookstorebackend.user.model.User;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "promotion_usages")
public class PromotionUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;
}
