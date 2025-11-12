package tetish_inn_backend.tetish_inn.modules.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tetish_inn_backend.tetish_inn.common.utils.ApiResponse;
import tetish_inn_backend.tetish_inn.common.utils.PaginatedResponse;
import tetish_inn_backend.tetish_inn.common.utils.PaginationRequest;
import tetish_inn_backend.tetish_inn.modules.auth.AuthResponse;
import tetish_inn_backend.tetish_inn.modules.auth.RefreshTokenService;
import tetish_inn_backend.tetish_inn.modules.order.dto.BulkOrderRequestDTO;
import tetish_inn_backend.tetish_inn.modules.order.dto.OrderRequestDTO;
import tetish_inn_backend.tetish_inn.modules.order.dto.OrderResponseDTO;
import tetish_inn_backend.tetish_inn.modules.security.JwtService;
import tetish_inn_backend.tetish_inn.modules.snack.Snack;
import tetish_inn_backend.tetish_inn.modules.snack.SnackRepository;
import tetish_inn_backend.tetish_inn.modules.user.User;
import tetish_inn_backend.tetish_inn.modules.user.UserRepository;

import static tetish_inn_backend.tetish_inn.common.utils.GlobalCC.getCurrentUser;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final SnackRepository snackRepository;

    @Transactional
    public ResponseEntity<ApiResponse<Object>> saveOrUpdate(BulkOrderRequestDTO request) {
        User currentUser = getCurrentUser();
        User user = userRepository.findById(currentUser.getUsrId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        BigDecimal totalAmount = request.getOrders().stream()
                .map(OrderRequestDTO::getOrdTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (user.getUsrBalance().compareTo(totalAmount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        List<Order> savedOrders = new ArrayList<>();
        for (OrderRequestDTO orderRequest : request.getOrders()) {
            Order entity = orderMapper.toEntity(orderRequest);

            Snack snack = snackRepository.findById(orderRequest.getSnackId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Snack not found"));
            User vendor = userRepository.findById(snack.getSnkUser().getUsrId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));
            entity.setUser(user);
            entity.setSnack(snack);
            entity.setVendor(vendor);
            savedOrders.add(entity);
        }

        user.setUsrBalance(user.getUsrBalance().subtract(totalAmount));

        orderRepository.saveAll(savedOrders);

        refreshTokenService.revokeAllForUser(user.getUsrId());
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user.getUsrId().toString());
        Instant expiry = jwtService.getRefreshExpiration(refreshToken).toInstant();
        refreshTokenService.createRefreshToken(user, refreshToken, expiry);

        AuthResponse authResponse = new AuthResponse(
                accessToken,
                refreshToken,
                user.getUsrId()
        );

        return ResponseEntity.ok(ApiResponse.success("Orders saved successfully", authResponse));
    }

    public ResponseEntity<ApiResponse<Object>> getById(UUID id) {

        return orderRepository.findById(id)
                .map(e -> ResponseEntity.ok(ApiResponse.success("Order fetched", (Object) e)))
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<ApiResponse<Object>> delete(UUID id) {

        return orderRepository.findById(id)
                .map(e -> {
                    orderRepository.delete(e);
                    return ResponseEntity.ok(ApiResponse.success("Order deleted", null));
                }).orElse(ResponseEntity.notFound().build());
    }

    public PaginatedResponse<OrderResponseDTO> getAll(PaginationRequest request) {

        int start = Math.max(request.getStart(), 0);
        int limit = request.getLimit() > 0 ? request.getLimit() : 10;

        Pageable pageable = PageRequest.of(start / limit, limit, Sort.by("createdAt").descending());
        Page<Order> page = orderRepository.findAll(pageable);
        Page<OrderResponseDTO> mapped = page.map(orderMapper::toDto);

        return new PaginatedResponse<>(mapped.getContent(), mapped.getTotalPages());
    }
}
