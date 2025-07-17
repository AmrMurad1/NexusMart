package amrmurad.nexusmart.DTOs.cartDTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private Long cartID;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private Integer totalItems;

}
