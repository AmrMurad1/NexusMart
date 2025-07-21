package amrmurad.nexusmart.controllers;
import amrmurad.nexusmart.DTOs.cartDTOs.AddToCartRequest;
import amrmurad.nexusmart.DTOs.cartDTOs.CartResponse;
import amrmurad.nexusmart.DTOs.cartDTOs.UpdateCartItemRequest;
import amrmurad.nexusmart.services.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId){
        log.info("GET /api/cart/{} - Getting cart for user", userId);

        CartResponse cartResponse = cartService.getCartByUserId(userId);
        log.info("successfully retrieved cart for user: {}, total items: {}", userId, cartResponse.getTotalItems());

        return ResponseEntity.ok(cartResponse);

    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addToCart(@PathVariable Long userId, @RequestBody AddToCartRequest request) {

        CartResponse cartResponse = cartService.addToCart(userId, request);
        return ResponseEntity.ok(cartResponse);
    }

    @GetMapping("/{userId}/count")
    public ResponseEntity<Integer> itemCount(@PathVariable Long userId){

        int itemCount = cartService.getCartItemCount(userId);

        return ResponseEntity.ok(itemCount);
    }

    @PutMapping("/{userId}/items")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable Long userId, @RequestBody UpdateCartItemRequest request){
        CartResponse response = cartService.updateQuantity(userId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long userId,@PathVariable Long productId){
         cartService.removeFromCart(userId  , productId);

         return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId){
        cartService.clearCart(userId);

        return ResponseEntity.noContent().build();
    }

}
