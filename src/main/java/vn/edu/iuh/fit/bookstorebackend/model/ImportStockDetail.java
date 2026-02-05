package vn.edu.iuh.fit.bookstorebackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "import_stock_details")
public class ImportStockDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
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

}
