package tetish_inn_backend.tetish_inn.modules.order;

    import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tetish_inn_backend.tetish_inn.common.utils.ApiResponse;
import tetish_inn_backend.tetish_inn.common.utils.PaginatedResponse;
import tetish_inn_backend.tetish_inn.common.utils.PaginationRequest;
    import tetish_inn_backend.tetish_inn.modules.order.dto.BulkOrderRequestDTO;
    import tetish_inn_backend.tetish_inn.modules.order.dto.OrderRequestDTO;
import tetish_inn_backend.tetish_inn.modules.order.dto.OrderResponseDTO;

    @RestController
    @RequestMapping("/api/order")
    @RequiredArgsConstructor
    public class OrderController {

        private final OrderService service;

        @PostMapping("/save-or-update")
        public ResponseEntity<ApiResponse<Object>> saveOrUpdate(@RequestBody BulkOrderRequestDTO request) {
            return service.saveOrUpdate(request);
        }

        @PostMapping("/all")
        public PaginatedResponse<OrderResponseDTO> getAll(@RequestBody PaginationRequest request) {
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
