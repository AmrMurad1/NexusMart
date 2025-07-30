package amrmurad.nexusmart.DTOs.orderDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderResponse {

    private Long orderId;
    private String paymentClientSecret;
    private String paymentReference;

}
