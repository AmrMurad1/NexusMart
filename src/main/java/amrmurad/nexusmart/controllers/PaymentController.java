package amrmurad.nexusmart.controllers;

import amrmurad.nexusmart.entities.Payment;
import amrmurad.nexusmart.enums.PaymentMethod;
import amrmurad.nexusmart.services.StripePaymentService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final StripePaymentService stripePaymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentIntent> createPaymentIntent(
            @RequestParam Long amount,
            @RequestParam String currency,
            @RequestParam String orderId,
            @RequestParam PaymentMethod paymentMethod
    ) {
        PaymentIntent paymentIntent = stripePaymentService.createPaymentIntent(amount, currency, orderId, paymentMethod);
        return ResponseEntity.ok(paymentIntent);
    }

    @PostMapping("/confirm")
    public ResponseEntity<PaymentIntent> confirmPayment(@RequestParam String paymentIntentId) {
        PaymentIntent confirmed = stripePaymentService.confirmPayment(paymentIntentId);
        return ResponseEntity.ok(confirmed);
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        Event event = stripePaymentService.handleWebhookEvent(payload, sigHeader);
        return ResponseEntity.ok("Webhook event processed: " + event.getType());
    }

    @GetMapping("/by-order")
    public ResponseEntity<Payment> getPaymentByOrderId(@RequestParam Integer orderId) {
        Optional<Payment> payment = stripePaymentService.getPaymentByOrderId(orderId);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/by-reference")
    public ResponseEntity<Payment> getPaymentByReference(@RequestParam String reference) {
        Optional<Payment> payment = stripePaymentService.getPaymentByReference(reference);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/intent/{id}")
    public ResponseEntity<PaymentIntent> getPaymentIntent(@PathVariable String id) {
        PaymentIntent paymentIntent = stripePaymentService.getPaymentIntent(id);
        return ResponseEntity.ok(paymentIntent);
    }
}
