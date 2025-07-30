package amrmurad.nexusmart.DTOs.orderDTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class OrderCalculationDTO {
    private List<String> stockIssues; // Messages about insufficient stock items
    private BigDecimal totalAmount;
    private boolean allItemsInStock;
}
