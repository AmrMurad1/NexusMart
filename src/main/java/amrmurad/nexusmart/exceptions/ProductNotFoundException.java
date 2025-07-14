package amrmurad.nexusmart.exceptions;

public class ProductNotFoundException extends RuntimeException{

    public ProductNotFoundException (Long id){
        super("product not found with id: "+ id);
    }
}
