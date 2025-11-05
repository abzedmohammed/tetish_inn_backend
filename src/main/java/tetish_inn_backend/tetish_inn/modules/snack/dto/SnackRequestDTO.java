package tetish_inn_backend.tetish_inn.modules.snack.dto;

import lombok.Data;
import tetish_inn_backend.tetish_inn.common.enums.SnackType;

import java.math.BigDecimal;

@Data
public class SnackRequestDTO {
    private String snkName;
    private BigDecimal snkPrice;
    private String snkImageUrl;
    private String snkDescription;
    private SnackType snkType;
}
