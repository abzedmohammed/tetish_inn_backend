package tetish_inn_backend.tetish_inn.modules.snack;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tetish_inn_backend.tetish_inn.common.utils.ApiResponse;
import tetish_inn_backend.tetish_inn.common.utils.PaginatedResponse;
import tetish_inn_backend.tetish_inn.common.utils.PaginationRequest;
import tetish_inn_backend.tetish_inn.modules.snack.dto.SnackRequestDTO;
import tetish_inn_backend.tetish_inn.modules.snack.dto.SnackResponseDTO;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/snacks")
public class SnackController {
    private final SnackService snackService;

    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Object>> saveSnack(@RequestBody SnackRequestDTO request) {
        return snackService.create(request);
    }

    @PostMapping("/all")
    public ResponseEntity<ApiResponse<PaginatedResponse<SnackResponseDTO>>> fetchSnacks(@RequestBody PaginationRequest request) {
        PaginatedResponse<SnackResponseDTO> result = snackService.fetchSnacks(request);
        return ResponseEntity.ok(
                ApiResponse.success(result)
        );
    }
}
