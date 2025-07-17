package amrmurad.nexusmart.DTOs.cartDTOs;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddToCartRequest {
   @NotNull
   @Positive
   private Long productId;

   @NotNull
   @Min(1)
    private Integer quantity;
}