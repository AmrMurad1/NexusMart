package amrmurad.nexusmart.controllers;

import amrmurad.nexusmart.DTOs.orderDTOs.PlaceOrderResponse;
import amrmurad.nexusmart.entities.Order;
import amrmurad.nexusmart.enums.OrderStatus;
import amrmurad.nexusmart.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;


    @PostMapping("/place/{userId}")
    public ResponseEntity<PlaceOrderResponse> placeOrder(@PathVariable Integer userId) {
        log.info("Placing order for user: {}", userId);

        try {
            PlaceOrderResponse response = orderService.placeOrder(userId);
            log.info("Order placed successfully for user: {} with order ID: {}", userId, response.getOrderId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Failed to place order for user: {}", userId, e);
            throw e;
        }
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Integer userId) {
        log.info("Fetching orders for user: {}", userId);

        List<Order> orders = orderService.getUserOrders(userId);
        log.info("Found {} orders for user: {}", orders.size(), userId);
        return ResponseEntity.ok(orders);
    }


    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        log.info("Fetching all orders");

        List<Order> orders = orderService.getAllOrders();
        log.info("Found {} total orders", orders.size());
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        log.info("Fetching order with ID: {}", orderId);

        Order order = orderService.getOrderById(orderId);
        log.info("Found order: {}", orderId);
        return ResponseEntity.ok(order);
    }


    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, String>> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {

        log.info("Updating status for order: {}", orderId);

        String statusStr = request.get("status");
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Status is required"));
        }

        try {
            OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
            orderService.updateOrderStatus(orderId, newStatus);

            log.info("Successfully updated order {} status to: {}", orderId, newStatus);
            return ResponseEntity.ok(Map.of(
                    "message", "Order status updated successfully",
                    "orderId", orderId.toString(),
                    "newStatus", newStatus.toString()
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid order status provided: {}", statusStr);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid order status: " + statusStr));
        } catch (Exception e) {
            log.error("Failed to update order status for order: {}", orderId, e);
            throw e;
        }
    }


    @PostMapping("/payment/success")
    public ResponseEntity<Map<String, String>> handlePaymentSuccess(
            @RequestBody Map<String, String> request) {

        String paymentReference = request.get("paymentReference");
        log.info("Processing successful payment for reference: {}", paymentReference);

        if (paymentReference == null || paymentReference.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Payment reference is required"));
        }

        try {
            orderService.handlePaymentSuccess(paymentReference);
            log.info("Successfully processed payment success for reference: {}", paymentReference);
            return ResponseEntity.ok(Map.of(
                    "message", "Payment success processed",
                    "paymentReference", paymentReference
            ));
        } catch (Exception e) {
            log.error("Failed to process payment success for reference: {}", paymentReference, e);
            throw e;
        }
    }


    @PostMapping("/payment/failure")
    public ResponseEntity<Map<String, String>> handlePaymentFailure(
            @RequestBody Map<String, String> request) {

        String paymentReference = request.get("paymentReference");
        log.info("Processing failed payment for reference: {}", paymentReference);

        if (paymentReference == null || paymentReference.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Payment reference is required"));
        }

        try {
            orderService.handlePaymentFailure(paymentReference);
            log.info("Successfully processed payment failure for reference: {}", paymentReference);
            return ResponseEntity.ok(Map.of(
                    "message", "Payment failure processed",
                    "paymentReference", paymentReference
            ));
        } catch (Exception e) {
            log.error("Failed to process payment failure for reference: {}", paymentReference, e);
            throw e;
        }
    }


    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "OrderController",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}