package amrmurad.nexusmart.services;

import amrmurad.nexusmart.entities.Product;
import amrmurad.nexusmart.exceptions.ProductNotFoundException;
import amrmurad.nexusmart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> gitAllProducts(){
        return productRepository.findAll();
    }

    public Product getProductById(Long id ){
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public Product createProduct(Product product){
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails){
        Product existingProduct = getProductById(id);

        existingProduct.setName(productDetails.getName());
        existingProduct.setDescription(productDetails.getDescription());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setStockQuantity(productDetails.getStockQuantity());

        return productRepository.save(productDetails);
    }

    public void deleteProduct(Long id){
        Product product = getProductById(id);

        productRepository.delete(product);
    }

}
