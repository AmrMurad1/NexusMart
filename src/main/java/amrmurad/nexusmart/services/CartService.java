package amrmurad.nexusmart.services;

import amrmurad.nexusmart.DTOs.cartDTOs.AddToCartRequest;
import amrmurad.nexusmart.DTOs.cartDTOs.CartItemResponse;
import amrmurad.nexusmart.DTOs.cartDTOs.CartResponse;
import amrmurad.nexusmart.DTOs.cartDTOs.UpdateCartItemRequest;
import amrmurad.nexusmart.entities.*;
import amrmurad.nexusmart.exceptions.ProductNotFoundException;
import amrmurad.nexusmart.repository.CartItemsRepository;
import amrmurad.nexusmart.repository.CartRepository;
import amrmurad.nexusmart.repository.ProductRepository;
import amrmurad.nexusmart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemsRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartResponse getCartByUserId(Long userId) {
        log.info("Getting cart for user id: {}", userId);

        Cart cart = cartRepository.findByUserId(userId)
                .map(existingCart -> {
                    log.debug("found existing cart with id: {}, for user with id: {}", existingCart.getId(), userId);
                    return existingCart;
                })
                .orElseGet(() -> {
                    log.info("cart not found with user id: {}, create new cart", userId);
                    return createCartForUser(userId);
                });
        return convertToCartResponse(cart);
    }

    private Cart createCartForUser(Long userId) {
        log.info("create new cart for user id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("user not found with id: {}", userId);
                    return new UsernameNotFoundException("user not found");
                });

        Cart cart = new Cart();
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {

        log.info("adding to cart - user: {}, product: {}, quantity: {}", userId, request.getProductId(), request.getQuantity());

        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.error("product not found with id: {}", request.getProductId());
                    return new ProductNotFoundException(request.getProductId());
                });

        cartItemRepository.findByCartAndProduct(cart, product)
                .ifPresentOrElse(existingItem -> {
                    int oldQuantity = existingItem.getQuantity();
                    int newQuantity = oldQuantity + request.getQuantity();
                    existingItem.setQuantity(newQuantity);
                    cartItemRepository.save(existingItem);


                    log.info("Updated existing cart item - ID: {}, Old quantity: {}, New quantity: {}",
                            existingItem.getId(), oldQuantity, newQuantity);
                },
                        () -> {
                    CartItem newItem = new CartItem();
                    newItem.setCart(cart);
                    newItem.setProduct(product);
                    newItem.setQuantity(request.getQuantity());

                    CartItem savedItem = cartItemRepository.save(newItem);
                            log.info("Created new cart item - ID: {}, Product: {}, Quantity: {}",
                                    savedItem.getId(), request.getProductId(), request.getQuantity());
                            }
                        );

        return convertToCartResponse(cart);
    }


    @Transactional
    public void removeFromCart(Long userId, Long productId){
        log.info("Remove form cart - user: {}, product: {}", userId, productId);

        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("product not found ID: {}", productId);
                    return new ProductNotFoundException(productId);
                });

        cartItemRepository.findByCartAndProduct(cart, product)
                .ifPresentOrElse(cartItem -> {
                    cartItemRepository.deleteByCart(cart);
                    log.info("Successfully removed cart item - ID: {}, Product: {}",
                            cartItem.getId(), productId);
                },
                () -> log.warn("Cart item not found for removal - User: {}, Product: {}",
                    userId, productId)
                  );
    }


    @Transactional
    public CartResponse updateQuantity(Long userId, UpdateCartItemRequest request) {

        log.info("Updating quantity - User: {}, Product: {}, New quantity: {}",
                userId, request.getProductId(), request.getQuantity());

        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", request.getProductId());
                    return new ProductNotFoundException(request.getProductId());
                });


        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElseThrow(() -> {
                    log.error("Cart item not found - User: {}, Product: {}", userId, request.getProductId());
                    return new RuntimeException("Item not found in cart");
                });

        if (request.getQuantity() <= 0) {
            cartItemRepository.delete(cartItem);
            log.info("Deleted cart item due to zero/negative quantity - ID: {}", cartItem.getId());
        } else {
            int oldQuantity = cartItem.getQuantity();
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);

            log.info("Updated cart item quantity - ID: {}, Old: {}, New: {}",
                    cartItem.getId(), oldQuantity, request.getQuantity());
        }

        return convertToCartResponse(cart);
    }


    public void clearCart(Long userId) {
        log.info("Clearing cart for user: {}", userId);

        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        int itemCount = items.size();

        cartItemRepository.deleteByCart(cart);

        log.info("Successfully cleared cart for user: {}, deleted {} items", userId, itemCount);
    }

    public int getCartItemCount(Long userId) {
        log.debug("Getting cart item count for user: {}", userId);

        Cart cart = getOrCreateCart(userId);
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
        int totalCount = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        log.debug("Cart item count for user {}: {}", userId, totalCount);
        return totalCount;
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> createCartForUser(userId));
    }

    private CartResponse convertToCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> itemResponses = items.stream()
                .map(this::convertToCartItemResponse)
                .collect(Collectors.toList());

        // Calculate total amount (price comes from product)
        BigDecimal totalAmount = items.stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate total items count
        Integer totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return new CartResponse(cart.getId(), itemResponses, totalAmount, totalItems);
    }

    private CartItemResponse convertToCartItemResponse(CartItem cartItem) {
        return CartItemResponse.builder()
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .price(cartItem.getProduct().getPrice())
                .quantity(cartItem.getQuantity())
                .build();
    }
}