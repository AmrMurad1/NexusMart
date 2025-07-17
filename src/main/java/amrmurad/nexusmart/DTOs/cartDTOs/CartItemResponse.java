package amrmurad.nexusmart.DTOs.cartDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Data
@Builder
@AllArgsConstructor
public class CartItemResponse {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
}
