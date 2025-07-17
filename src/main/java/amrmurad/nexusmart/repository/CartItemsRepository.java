package amrmurad.nexusmart.repository;

import amrmurad.nexusmart.DTOs.cartDTOs.CartResponse;
import amrmurad.nexusmart.entities.Cart;
import amrmurad.nexusmart.entities.CartItem;
import amrmurad.nexusmart.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemsRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    void deleteByCartId(Long cartId);
    void deleteByCart(Cart cart);
}
