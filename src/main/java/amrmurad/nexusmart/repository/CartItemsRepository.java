package amrmurad.nexusmart.repository;

import amrmurad.nexusmart.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemsRepository extends JpaRepository<Long, CartItem> {

    List<CartItem> findByCartId(Long cart_id);

    Optional<CartItem> findByCartIdAndProductId(Long cart_id, Long product_id);

    void deleteByCartId(Long cart_id);
}
