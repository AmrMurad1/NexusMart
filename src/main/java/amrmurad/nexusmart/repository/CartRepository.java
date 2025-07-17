package amrmurad.nexusmart.repository;

import amrmurad.nexusmart.entities.Cart;
import amrmurad.nexusmart.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Long, Cart> {

    Optional<Cart> findByUserName(User UserName);

    Optional<Cart> findByUserId(Long userId);
}
