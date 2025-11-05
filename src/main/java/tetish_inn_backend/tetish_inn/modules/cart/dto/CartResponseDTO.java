package tetish_inn_backend.tetish_inn.modules.cart.dto;

    import lombok.Data;
    import java.math.BigDecimal;
import java.util.UUID;

    @Data
    public class CartResponseDTO {
    private UUID id;
    private BigDecimal crtTotal;
    private BigDecimal crtQuantity;
    }
