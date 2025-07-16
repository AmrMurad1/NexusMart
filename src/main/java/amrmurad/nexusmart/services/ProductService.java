package amrmurad.nexusmart.services;

import amrmurad.nexusmart.entities.Product;
import amrmurad.nexusmart.exceptions.ProductNotFoundException;
import amrmurad.nexusmart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id ){
        if (id == null){
            throw new IllegalArgumentException("product ID cannot be null");
        }
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product createProduct(Product product){
        validateProductForCreation(product);

        if (productRepository.existsByNameIgnoreCase(product.getName())){
            throw new IllegalArgumentException("product with name '"+ product.getName() + "' already exist");
        }
        log.info("Creating new product: {}", product.getName());
        Product savedProduct = productRepository.save(product);
        log.info("Saved product with ID: {}", product.getId());
        return savedProduct;

    }


    public Product updateProduct(Long id, Product productDetails){
        Product existingProduct = getProductById(id);

        if (productDetails.getName() != null && !productDetails.getName().trim().isEmpty()){
            if (productRepository.existsByNameIgnoreCase(productDetails.getName()) &&
                    !existingProduct.getName().equalsIgnoreCase(productDetails.getName())) {
                throw new IllegalArgumentException("product with name '" + productDetails.getName() +"' already exist");
            }
            existingProduct.setName(productDetails.getName());
        }

        if (productDetails.getDescription() != null) {
            existingProduct.setDescription(productDetails.getDescription());
        }

        if (productDetails.getPrice() != null) {
            validatePrice(productDetails.getPrice());
            existingProduct.setPrice(productDetails.getPrice());
        }

        if (productDetails.getStockQuantity() != null) {
            validateStockQuantity(productDetails.getStockQuantity());
            existingProduct.setStockQuantity(productDetails.getStockQuantity());
        }

        log.info("Updating product with ID: {}", id);
        return productRepository.save(existingProduct);
    }
    public void deleteProduct(Long id){
        if (!productRepository.existsById(id)){
            throw new ProductNotFoundException(id);
        }
        log.info("Delete product with id {}", id );
        productRepository.deleteById(id);
    }
    private void validateProductForCreation(Product product) {
        if (product == null){
            throw new IllegalArgumentException("product cannot be null");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()){
            throw new IllegalArgumentException("product name is required");
        }
        validatePrice(product.getPrice());
        validateStockQuantity(product.getStockQuantity());
    }

    private void validateStockQuantity(Integer stockQuantity) {
        if (stockQuantity == null)
            throw new IllegalArgumentException("quantity cannot be null");
        if (stockQuantity < 0)
            throw new IllegalArgumentException("quantity cannot be negative");
    }

    private void validatePrice(BigDecimal price) {
        if (price == null){
            throw new IllegalArgumentException("price is required");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("price cannot be negative");
        }
    }


    @Transactional(readOnly = true)
    public List<Product> searchProductByName (String name){
        if (name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("name cannot be empty");
        }
        return productRepository.findByNameContainingIgnoreCase(name.trim());
    }

    @Transactional(readOnly = true)
    public List<Product> getProductByPriceRange(BigDecimal maxPrice, BigDecimal minPrice){
        if (minPrice == null || maxPrice == null){
            throw new IllegalArgumentException("price cannot be null");
        }
        if (minPrice.compareTo(maxPrice) > 0 ){
            throw new IllegalArgumentException("Minimum price cannot be greater than maximum price");
        }
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }
    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        if (threshold == null || threshold < 0) {
            throw new IllegalArgumentException("Threshold must be a non-negative number");
        }
        return productRepository.findByStockQuantityLessThan(threshold);
    }

    public Product updateStock(Long id, Integer newQuantity) {
        validateStockQuantity(newQuantity);
        Product product = getProductById(id);
        product.setStockQuantity(newQuantity);
        return productRepository.save(product);
    }

}



