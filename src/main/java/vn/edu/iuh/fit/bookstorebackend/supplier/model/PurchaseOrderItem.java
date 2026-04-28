package vn.edu.iuh.fit.bookstorebackend.supplier.model;
import vn.edu.iuh.fit.bookstorebackend.book.model.Book;
import vn.edu.iuh.fit.bookstorebackend.book.model.Variant;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "purchase_order_items")
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "import_price", nullable = false)
    private double importPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private Variant variant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;
}
