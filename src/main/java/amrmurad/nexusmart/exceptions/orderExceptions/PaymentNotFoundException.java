package amrmurad.nexusmart.exceptions.orderExceptions;

public class PaymentNotFoundException extends RuntimeException {

    public PaymentNotFoundException(String paymentReference) {
        super("Payment not found with reference: " + paymentReference);
    }
}
