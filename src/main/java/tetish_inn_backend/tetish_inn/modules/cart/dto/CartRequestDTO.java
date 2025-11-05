package tetish_inn_backend.tetish_inn.modules.cart.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartRequestDTO {
private BigDecimal crtTotal;
private BigDecimal crtQuantity;
}
