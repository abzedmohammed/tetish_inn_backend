package tetish_inn_backend.tetish_inn.modules.order;

    import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import tetish_inn_backend.tetish_inn.modules.order.dto.OrderRequestDTO;
import tetish_inn_backend.tetish_inn.modules.order.dto.OrderResponseDTO;

    @Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
    public interface OrderMapper {

        Order toEntity(OrderRequestDTO dto);

        void updateEntityFromDto(OrderRequestDTO dto, @MappingTarget Order entity);

        OrderResponseDTO toDto(Order entity);
    }
