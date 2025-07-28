package amrmurad.nexusmart.repository;

import amrmurad.nexusmart.entities.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {

    // Find items by order
    List<OrderItems> findByOrderId(Integer orderId);

    // Find items by product
    List<OrderItems> findByProductId(Integer productId);
}
