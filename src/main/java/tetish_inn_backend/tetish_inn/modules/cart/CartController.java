package tetish_inn_backend.tetish_inn.modules.cart;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tetish_inn_backend.tetish_inn.common.utils.ApiResponse;
import tetish_inn_backend.tetish_inn.common.utils.PaginatedResponse;
import tetish_inn_backend.tetish_inn.common.utils.PaginationRequest;
import tetish_inn_backend.tetish_inn.modules.cart.dto.CartRequestDTO;
import tetish_inn_backend.tetish_inn.modules.cart.dto.CartResponseDTO;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService service;

    @PostMapping("/save-or-update")
    public ResponseEntity<ApiResponse<Object>> saveOrUpdate(@RequestBody CartRequestDTO request) {
        return service.saveOrUpdate(null, request);
    }

    @PostMapping("/all")
    public PaginatedResponse<CartResponseDTO> getAll(@RequestBody PaginationRequest request) {
        return service.getAll(request);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable UUID id) {
        return service.delete(id);
    }
}
