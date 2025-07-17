package amrmurad.nexusmart.repository;

import amrmurad.nexusmart.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    // Query by the username property of the user field
    Optional<Cart> findByUserUsername(String username);

    Optional<Cart> findByUserId(Long userId);
}