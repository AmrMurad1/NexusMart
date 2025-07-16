package amrmurad.nexusmart.controllers;

import amrmurad.nexusmart.entities.Product;
import amrmurad.nexusmart.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
@ControllerAdvice
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;


    @RequestMapping
    public ResponseEntity<List<Product>> getAllProducts(){
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id){
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProductByName(@RequestParam String name){
        List<Product> products = productService.searchProductByName(name);
        return ResponseEntity.ok(products);
    }

    @GetMapping(value = "/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(@RequestParam BigDecimal minPrice,
                                                                 @RequestParam BigDecimal maxPrice){

        List<Product> products = productService.getProductByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStockProducts(@RequestParam Integer threshold){
        List<Product> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product){
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,@RequestBody Product productDetails){
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Product> updateStock(@PathVariable Long id, @RequestBody Integer newQuantity){
        Product updatedStock = productService.updateStock(id, newQuantity);
        return ResponseEntity.ok(updatedStock);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id){
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }


}
