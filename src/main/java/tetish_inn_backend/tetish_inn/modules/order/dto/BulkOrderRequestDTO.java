package tetish_inn_backend.tetish_inn.modules.order.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class BulkOrderRequestDTO {
    private List<OrderRequestDTO> orders;
}
