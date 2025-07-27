package amrmurad.nexusmart.repository;

import amrmurad.nexusmart.entities.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {

    // Find items by order
    List<OrderItems> findByOrderId(Integer orderId);

    // Find items by product
    List<OrderItems> findByProductId(Integer productId);
}
