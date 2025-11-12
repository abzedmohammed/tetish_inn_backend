package tetish_inn_backend.tetish_inn.modules.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class OrderResponseDTO {
    private UUID ordId;

    private BigDecimal ordAmount;

    private BigDecimal ordTotal;
}
