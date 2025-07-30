package amrmurad.nexusmart.repository;

import amrmurad.nexusmart.entities.Order;
import amrmurad.nexusmart.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Find orders by user
    List<Order> findByUserId(Integer userId);

    // Find orders by user ordered by creation date descending
    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);

    // Find orders by status
    List<Order> findByStatus(OrderStatus status);

    // Find orders by user and status
    List<Order> findByUserIdAndStatus(Integer userId, OrderStatus status);

    // Find orders within date range
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find user orders within date range
    List<Order> findByUserIdAndCreatedAtBetween(Integer userId, LocalDateTime startDate, LocalDateTime endDate);
}