package amrmurad.nexusmart.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Entity
@Data
@Table(name = "order_items")
public class OrderItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull
    @DecimalMin(value = "0.00", message = "Price must be positive")
    @Column(name = "price_at_purchase", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAtPurchase;

    // Relationship mappings
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;

}
