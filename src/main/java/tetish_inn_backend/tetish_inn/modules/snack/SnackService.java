package tetish_inn_backend.tetish_inn.modules.snack;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import tetish_inn_backend.tetish_inn.common.utils.ApiResponse;
import tetish_inn_backend.tetish_inn.common.utils.PaginatedResponse;
import tetish_inn_backend.tetish_inn.common.utils.PaginationRequest;
import tetish_inn_backend.tetish_inn.modules.snack.dto.SnackRequestDTO;
import tetish_inn_backend.tetish_inn.modules.snack.dto.SnackResponseDTO;
import tetish_inn_backend.tetish_inn.modules.snack.mapper.SnackMapper;

@Service
@AllArgsConstructor
@Slf4j
public class SnackService {
    private final SnackRepository snackRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(SnackRequestDTO request) {
        Snack snack = SnackMapper.toEntity(request);
        Snack savedSnack = snackRepository.save(snack);
        SnackResponseDTO responseDTO = SnackMapper.toDTO(savedSnack);

        return ResponseEntity.ok(ApiResponse.success(
                "Snack created successfully",
                responseDTO
        ));
    }

    @PostMapping
    public PaginatedResponse<SnackResponseDTO> fetchSnacks(PaginationRequest request) {
        int start = Math.max(request.getStart(), 0);
        int limit = request.getLimit() > 0 ? request.getLimit() : 10;

        Pageable pageable = PageRequest.of(start / limit, limit, Sort.by("createdAt").descending());
        Page<SnackResponseDTO> result = snackRepository.findAll(pageable)
                .map(SnackMapper::toDTO);

        return new PaginatedResponse<>(result.getContent(), result.getTotalPages());
    }

}
