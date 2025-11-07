package tetish_inn_backend.tetish_inn.modules.cart;

import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import tetish_inn_backend.tetish_inn.common.utils.ApiResponse;
import tetish_inn_backend.tetish_inn.common.utils.PaginatedResponse;
import tetish_inn_backend.tetish_inn.common.utils.PaginationRequest;
import tetish_inn_backend.tetish_inn.modules.cart.dto.CartRequestDTO;
import tetish_inn_backend.tetish_inn.modules.cart.dto.CartResponseDTO;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    public ResponseEntity<ApiResponse<Object>> saveOrUpdate(UUID id, CartRequestDTO request) {

        Cart entity;
        if (id != null) {
            Optional<Cart> existing = cartRepository.findById(id);
            if (existing.isPresent()) {
                entity = existing.get();
                cartMapper.updateEntityFromDto(request, entity);
            } else {
                return ResponseEntity.notFound().build();
            }
        } else {
            entity = cartMapper.toEntity(request);
        }

        cartRepository.save(entity);
        return ResponseEntity.ok(ApiResponse.success("Cart saved successfully", (Object) entity));
    }

    public ResponseEntity<ApiResponse<Object>> getById(UUID id) {

        return cartRepository.findById(id)
                .map(e -> ResponseEntity.ok(ApiResponse.success("Cart fetched", (Object) e)))
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<ApiResponse<Object>> delete(UUID id) {

        return cartRepository.findById(id)
                .map(e -> {
                    cartRepository.delete(e);
                    return ResponseEntity.ok(ApiResponse.success("Cart deleted", null));
                }).orElse(ResponseEntity.notFound().build());
    }

    public PaginatedResponse<CartResponseDTO> getAll(PaginationRequest request) {

        int start = Math.max(request.getStart(), 0);
        int limit = request.getLimit() > 0 ? request.getLimit() : 10;

        Pageable pageable = PageRequest.of(start / limit, limit, Sort.by("createdAt").descending());
        Page<Cart> page = cartRepository.findAll(pageable);
        Page<CartResponseDTO> mapped = page.map(cartMapper::toDto);

        return new PaginatedResponse<>(mapped.getContent(), mapped.getTotalPages());
    }
}
