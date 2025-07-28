package amrmurad.nexusmart.services;
import amrmurad.nexusmart.entities.Payment;
import amrmurad.nexusmart.enums.PaymentMethod;
import amrmurad.nexusmart.enums.PaymentStatus;
import amrmurad.nexusmart.repository.PaymentRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class StripePaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }


    public PaymentIntent createPaymentIntent(Long amountInCents, String currency, String orderId, PaymentMethod paymentMethod) {
        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("order_id", orderId);
            metadata.put("integration", "nexusmart");

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency)
                    .putAllMetadata(metadata)
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Create or update Payment entity
            Payment payment = paymentRepository.findByOrderId(Integer.valueOf(orderId))
                    .orElse(new Payment());

            payment.setOrderId(Integer.valueOf(orderId));
            payment.setPaymentProvider(paymentMethod);
            payment.setPaymentStatus(PaymentStatus.PENDING);
            payment.setPaymentReference(paymentIntent.getId());

            paymentRepository.save(payment);

            return paymentIntent;

        } catch (StripeException e) {
            throw new RuntimeException("Payment intent creation failed", e);
        }
    }


    public Event handleWebhookEvent(String payload, String sigHeader) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "payment_intent.created":
                    handlePaymentIntentCreated(event);
                    break;
            }

            return event;

        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Webhook signature verification failed", e);
        } catch (Exception e) {
            throw new RuntimeException("Webhook event processing failed", e);
        }
    }


    public PaymentIntent confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            PaymentIntent confirmedPayment = paymentIntent.confirm();

            Optional<Payment> paymentOpt = paymentRepository.findByPaymentReference(paymentIntentId);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                if ("succeeded".equals(confirmedPayment.getStatus())) {
                    payment.setPaymentStatus(PaymentStatus.COMPLETED);
                    payment.setPaidAt(LocalDateTime.now());
                } else if ("requires_action".equals(confirmedPayment.getStatus()) ||
                        "requires_confirmation".equals(confirmedPayment.getStatus())) {
                    payment.setPaymentStatus(PaymentStatus.PENDING);
                }
                paymentRepository.save(payment);
            }

            return confirmedPayment;

        } catch (StripeException e) {
            throw new RuntimeException("Payment confirmation failed", e);
        }
    }

    public PaymentIntent getPaymentIntent(String paymentIntentId) {
        try {
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve payment intent", e);
        }
    }


    public Optional<Payment> getPaymentByOrderId(Integer orderId) {
        return paymentRepository.findByOrderId(orderId);
    }


    public Optional<Payment> getPaymentByReference(String paymentReference) {
        return paymentRepository.findByPaymentReference(paymentReference);
    }

    // Private helper methods for handling specific webhook events

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (paymentIntent != null) {
            String orderId = paymentIntent.getMetadata().get("order_id");

            // Update Payment entity
            Optional<Payment> paymentOpt = paymentRepository.findByOrderId(Integer.valueOf(orderId));
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setPaymentStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (paymentIntent != null) {
            String orderId = paymentIntent.getMetadata().get("order_id");

            // Update Payment entity
            Optional<Payment> paymentOpt = paymentRepository.findByOrderId(Integer.valueOf(orderId));
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setPaidAt(null); // Clear paid date on failure
                paymentRepository.save(payment);
            }
        }
    }

    private void handlePaymentIntentCreated(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (paymentIntent != null) {
            String orderId = paymentIntent.getMetadata().get("order_id");

            // Update Payment entity if it exists
            Optional<Payment> paymentOpt = paymentRepository.findByOrderId(Integer.valueOf(orderId));
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                payment.setPaymentReference(paymentIntent.getId());
                payment.setPaymentStatus(PaymentStatus.PENDING);
                paymentRepository.save(payment);
            }
        }
    }
}