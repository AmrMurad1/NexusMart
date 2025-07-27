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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemsRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    // Helper methods for creating test entities
    private User createTestUser(Long userId) {
        User user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        return user;
    }

    private Cart createTestCart(Long cartId, Long userId) {
        Cart cart = new Cart();
        cart.setId(cartId);
        cart.setUser(createTestUser(userId));
        return cart;
    }

    private Product createTestProduct(Long productId, String name, BigDecimal price) {
        Product product = new Product();
        product.setId(productId);
        product.setName(name);
        product.setPrice(price);
        return product;
    }

    private CartItem createTestCartItem(Long itemId, Cart cart, Product product, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setId(itemId);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    // Tests for getCartByUserId()
    @Test
    void getCartByUserId_WhenCartExists_ShouldReturnCartResponse() {
        // Given
        Long userId = 1L;
        Long cartId = 100L;
        Cart existingCart = createTestCart(cartId, userId);

        Product product1 = createTestProduct(1L, "Product 1", BigDecimal.valueOf(10.00));
        Product product2 = createTestProduct(2L, "Product 2", BigDecimal.valueOf(15.00));

        CartItem item1 = createTestCartItem(1L, existingCart, product1, 2);
        CartItem item2 = createTestCartItem(2L, existingCart, product2, 1);

        List<CartItem> cartItems = List.of(item1, item2);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartItemRepository.findByCartId(cartId)).thenReturn(cartItems);

        // When
        CartResponse result = cartService.getCartByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(cartId, result.getCartID());
        assertEquals(2, result.getItems().size());
        assertEquals(3, result.getTotalItems()); // 2 + 1
        assertEquals(BigDecimal.valueOf(35.00), result.getTotalAmount()); // (10*2) + (15*1)

        // Verify repository interactions
        verify(cartRepository).findByUserId(userId);
        verify(cartItemRepository).findByCartId(cartId);
        verifyNoInteractions(userRepository); // Should not create new cart
        verifyNoMoreInteractions(cartRepository); // Should not save new cart
    }

    @Test
    void getCartByUserId_WhenCartDoesNotExist_ShouldCreateNewCartAndReturnResponse() {
        // Given
        Long userId = 1L;
        Long newCartId = 200L;
        User user = createTestUser(userId);
        Cart newCart = createTestCart(newCartId, userId);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);
        when(cartItemRepository.findByCartId(newCartId)).thenReturn(List.of()); // Empty cart

        // When
        CartResponse result = cartService.getCartByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(newCartId, result.getCartID());
        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotalItems());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());

        // Verify repository interactions
        verify(cartRepository).findByUserId(userId);
        verify(userRepository).findById(userId);
        verify(cartRepository).save(any(Cart.class));
        verify(cartItemRepository).findByCartId(newCartId);
    }

    @Test
    void getCartByUserId_WhenCartDoesNotExistAndUserNotFound_ShouldThrowUsernameNotFoundException() {
        // Given
        Long userId = 999L;

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> cartService.getCartByUserId(userId)
        );

        assertEquals("user not found", exception.getMessage());

        // Verify repository interactions
        verify(cartRepository).findByUserId(userId);
        verify(userRepository).findById(userId);
        verify(cartRepository, never()).save(any(Cart.class));
        verify(cartItemRepository, never()).findByCartId(any());
    }

    @Test
    void getCartByUserId_WhenCartExistsButEmpty_ShouldReturnEmptyCartResponse() {
        // Given
        Long userId = 1L;
        Long cartId = 100L;
        Cart existingCart = createTestCart(cartId, userId);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartItemRepository.findByCartId(cartId)).thenReturn(List.of()); // Empty list

        // When
        CartResponse result = cartService.getCartByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(cartId, result.getCartID());
        assertTrue(result.getItems().isEmpty());
        assertEquals(0, result.getTotalItems());
        assertEquals(BigDecimal.ZERO, result.getTotalAmount());

        // Verify repository interactions
        verify(cartRepository).findByUserId(userId);
        verify(cartItemRepository).findByCartId(cartId);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getCartByUserId_WithSingleItem_ShouldCalculateCorrectTotals() {
        // Given
        Long userId = 1L;
        Long cartId = 100L;
        Cart existingCart = createTestCart(cartId, userId);

        Product product = createTestProduct(1L, "Single Product", BigDecimal.valueOf(25.50));
        CartItem item = createTestCartItem(1L, existingCart, product, 3);

        List<CartItem> cartItems = List.of(item);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(existingCart));
        when(cartItemRepository.findByCartId(cartId)).thenReturn(cartItems);

        // When
        CartResponse result = cartService.getCartByUserId(userId);

        // Then
        assertNotNull(result);
        assertEquals(cartId, result.getCartID());
        assertEquals(1, result.getItems().size());
        assertEquals(3, result.getTotalItems());
        assertEquals(BigDecimal.valueOf(76.50), result.getTotalAmount()); // 25.50 * 3

        // Verify item response
        CartItemResponse itemResponse = result.getItems().get(0);
        assertEquals(1L, itemResponse.getProductId());
        assertEquals("Single Product", itemResponse.getProductName());
        assertEquals(BigDecimal.valueOf(25.50), itemResponse.getPrice());
        assertEquals(3, itemResponse.getQuantity());

        // Verify repository interactions
        verify(cartRepository).findByUserId(userId);
        verify(cartItemRepository).findByCartId(cartId);
    }
}