package amrmurad.nexusmart.exceptions.orderExceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }

}
