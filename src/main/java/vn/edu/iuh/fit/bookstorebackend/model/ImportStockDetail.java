package vn.edu.iuh.fit.bookstorebackend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "import_stock_details")
public class ImportStockDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "import_price", nullable = false)
    private double importPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_stock_id", nullable = false)
    private ImportStock importStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private Variant variant;

    @OneToMany(mappedBy = "importStockDetail", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Batch> batches; // Một ImportStockDetail có thể tạo nhiều Batch

}
