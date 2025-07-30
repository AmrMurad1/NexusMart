package amrmurad.nexusmart.services;

import amrmurad.nexusmart.DTOs.orderDTOs.OrderCalculationDTO;
import amrmurad.nexusmart.DTOs.orderDTOs.PlaceOrderResponse;
import amrmurad.nexusmart.entities.*;
import amrmurad.nexusmart.enums.OrderStatus;
import amrmurad.nexusmart.enums.PaymentMethod;
import amrmurad.nexusmart.enums.PaymentStatus;
import amrmurad.nexusmart.exceptions.*;
import amrmurad.nexusmart.exceptions.orderExceptions.EmptyCartException;
import amrmurad.nexusmart.exceptions.orderExceptions.InsufficientStockException;
import amrmurad.nexusmart.exceptions.orderExceptions.OrderNotFoundException;
import amrmurad.nexusmart.exceptions.orderExceptions.PaymentNotFoundException;
import amrmurad.nexusmart.repository.*;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemsRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StripePaymentService stripePaymentService;

    // ============= PUBLIC METHODS =============

    @Transactional
    public PlaceOrderResponse placeOrder(Integer userId) {
        // 1. Validate cart and calculate order
        OrderCalculationDTO calculation = validateAndCalculateOrder(userId);
        if (!calculation.isAllItemsInStock()) {
            throw new InsufficientStockException("Insufficient stock: " + String.join(", ", calculation.getStockIssues()));
        }

        // 2. Get cart items
        List<CartItem> cartItems = getCartItems(userId);

        // 3. Create Order record
        Order order = createOrderRecord(userId, calculation.getTotalAmount());

        // 4. Create OrderItems
        createOrderItems(order, cartItems);

        // 5. Create Stripe Payment Intent
        PaymentIntent paymentIntent = stripePaymentService.createPaymentIntent(
                calculation.getTotalAmount().multiply(new BigDecimal("100")).longValue(), // Convert to cents
                "usd", // or get from config
                order.getId().toString(),
                PaymentMethod.CREDIT_CARD
        );

        // 6. Decrement product stock
        decrementProductStock(cartItems);

        // 7. Clear user cart
        clearUserCart(userId);

        // 8. Extract client secret and return response
        String clientSecret = paymentIntent.getClientSecret();

        PlaceOrderResponse response = PlaceOrderResponse.builder()
                .orderId(order.getId())
                .paymentClientSecret(clientSecret)
                .paymentReference(paymentIntent.getId())
                .build();

        return response;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("Order status cannot be null");
        }

        Order order = findOrderById(orderId);
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional
    public void handlePaymentSuccess(String paymentReference) {
        if (paymentReference == null || paymentReference.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment reference cannot be null or empty");
        }

        Payment payment = findPaymentByReference(paymentReference);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        Order order = findOrderById(payment.getOrderId().longValue());
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Transactional
    public void handlePaymentFailure(String paymentReference) {
        if (paymentReference == null || paymentReference.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment reference cannot be null or empty");
        }

        Payment payment = findPaymentByReference(paymentReference);
        payment.setPaymentStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        Order order = findOrderById(payment.getOrderId().longValue());
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Restore product stock
        restoreProductStock(order);
    }

    public List<Order> getUserOrders(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        // Verify user exists
        userRepository.findById(userId.longValue())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }

        return findOrderById(orderId);
    }

    // ============= PRIVATE HELPER METHODS =============

    private OrderCalculationDTO validateAndCalculateOrder(Integer userId) {
        // Get cart items
        List<CartItem> cartItems = getCartItems(userId);

        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Cart is empty");
        }

        // Check stock availability
        List<String> stockIssues = checkStockAvailability(cartItems);
        boolean allItemsInStock = stockIssues.isEmpty();

        // Calculate total
        BigDecimal totalAmount = calculateOrderTotal(cartItems);

        return OrderCalculationDTO.builder()
                .stockIssues(stockIssues)
                .totalAmount(totalAmount)
                .allItemsInStock(allItemsInStock)
                .build();
    }

    private List<CartItem> getCartItems(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId.longValue())
                .orElseThrow(() -> new EmptyCartException("Cart not found for user: " + userId));

        return cartItemsRepository.findByCartId(cart.getId());
    }

    private List<String> checkStockAvailability(List<CartItem> cartItems) {
        List<String> stockIssues = new ArrayList<>();

        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            int requestedQuantity = item.getQuantity();
            int availableStock = product.getStockQuantity();

            if (availableStock < requestedQuantity) {
                String issue = String.format("Product '%s' has insufficient stock. Requested: %d, Available: %d",
                        product.getName(), requestedQuantity, availableStock);
                stockIssues.add(issue);
            }
        }

        return stockIssues;
    }

    private BigDecimal calculateOrderTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Order createOrderRecord(Integer userId, BigDecimal totalAmount) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    private void createOrderItems(Order order, List<CartItem> cartItems) {
        for (CartItem cartItem : cartItems) {
            OrderItems orderItem = new OrderItems();
            orderItem.setOrderId(order.getId().intValue());
            orderItem.setProductId(cartItem.getProduct().getId().intValue());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(cartItem.getProduct().getPrice());

            orderItemsRepository.save(orderItem);
        }
    }

    private Payment createPaymentRecord(Order order, String paymentReference) {
        log.debug("Creating payment record for order: {}", order.getId());

        Payment payment = new Payment();
        payment.setOrderId(order.getId().intValue());
        payment.setPaymentProvider(PaymentMethod.CREDIT_CARD);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentReference(paymentReference);

        Payment savedPayment = paymentRepository.save(payment);
        log.debug("Created payment record with ID: {}", savedPayment.getId());
        return savedPayment;
    }

    private void decrementProductStock(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            int oldStock = product.getStockQuantity();
            int newStock = oldStock - item.getQuantity();

            if (newStock < 0) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }

            product.setStockQuantity(newStock);
            productRepository.save(product);
        }
    }

    private void restoreProductStock(Order order) {
        List<OrderItems> orderItems = orderItemsRepository.findByOrderId(order.getId().intValue());

        for (OrderItems item : orderItems) {
            Product product = productRepository.findById(item.getProductId().longValue())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId().longValue()));

            int oldStock = product.getStockQuantity();
            int newStock = oldStock + item.getQuantity();
            product.setStockQuantity(newStock);
            productRepository.save(product);
        }
    }

    private void clearUserCart(Integer userId) {
        Cart cart = cartRepository.findByUserId(userId.longValue())
                .orElseThrow(() -> new EmptyCartException("Cart not found for user: " + userId));

        cartItemsRepository.deleteByCart(cart);
    }

    private Payment findPaymentByReference(String paymentReference) {
        return paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentReference));
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));
    }
}