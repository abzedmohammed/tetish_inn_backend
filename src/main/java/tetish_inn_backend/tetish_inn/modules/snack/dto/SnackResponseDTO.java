package tetish_inn_backend.tetish_inn.modules.snack.dto;

import lombok.Data;
import tetish_inn_backend.tetish_inn.common.enums.SnackType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SnackResponseDTO {
    private UUID snkId;
    private String snkName;
    private BigDecimal snkPrice;
    private String snkImageUrl;
    private String snkDescription;
    private SnackType snkType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

}
