package amrmurad.nexusmart.repository;

import amrmurad.nexusmart.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<Product> id(Long id);

    List<Product> findByStockQuantityLessThan(Integer quantity);
    boolean existsByNameIgnoreCase(String name);



}
