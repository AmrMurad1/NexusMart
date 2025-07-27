package amrmurad.nexusmart.entities;

import amrmurad.nexusmart.enums.PaymentMethod;
import amrmurad.nexusmart.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@RequiredArgsConstructor
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "order_id", nullable = false, unique = true)
    private Integer orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", length = 100)
    private PaymentMethod paymentProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50)
    private PaymentStatus paymentStatus;

    @Size(max = 255, message = "Payment reference cannot exceed 255 characters")
    @Column(name = "payment_reference", length = 255)
    private String paymentReference;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    // Relationship mapping
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;
}
