package amrmurad.nexusmart.repository;

import amrmurad.nexusmart.entities.Payment;
import amrmurad.nexusmart.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Find payment by order
    Optional<Payment> findByOrderId(Integer orderId);

    // Find payments by status
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    // Find payments within date range
    List<Payment> findByPaidAtBetween(LocalDateTime startDate, LocalDateTime endDate);

}
