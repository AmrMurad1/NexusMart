package amrmurad.nexusmart.repository;

import amrmurad.nexusmart.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    Optional<Product> findById(Long id);

    List<Product> findByStockQuantityLessThan(Integer quantity);
    boolean existsByNameIgnoreCase(String name);



}
