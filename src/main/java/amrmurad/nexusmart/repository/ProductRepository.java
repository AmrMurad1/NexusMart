package amrmurad.nexusmart.repository;

import amrmurad.nexusmart.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    List<Product> id(Long id);

    List<Product> findByStockQuantityLessThan(Integer quantity);
    List<Product> findByStockQuantityGreaterThan(Integer quantity);
    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();

}
