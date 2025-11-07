package tetish_inn_backend.tetish_inn.modules.cart.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CartRequestDTO {
    private BigDecimal crtAmount;
    private Integer crtQuantity;
    private UUID snackId;
    private UUID userId;
}
