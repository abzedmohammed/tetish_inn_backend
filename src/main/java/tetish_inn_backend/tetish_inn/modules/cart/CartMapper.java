package tetish_inn_backend.tetish_inn.modules.cart;

    import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import tetish_inn_backend.tetish_inn.modules.cart.Cart;
import tetish_inn_backend.tetish_inn.modules.cart.dto.CartRequestDTO;
import tetish_inn_backend.tetish_inn.modules.cart.dto.CartResponseDTO;

    @Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
    public interface CartMapper {

        Cart toEntity(CartRequestDTO dto);

        void updateEntityFromDto(CartRequestDTO dto, @MappingTarget Cart entity);

        CartResponseDTO toDto(Cart entity);
    }
