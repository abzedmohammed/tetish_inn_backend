package tetish_inn_backend.tetish_inn.modules.user;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import tetish_inn_backend.tetish_inn.common.utils.ApiResponse;
import tetish_inn_backend.tetish_inn.modules.order.OrderRepository;
import tetish_inn_backend.tetish_inn.modules.order.dto.OrderRequestDTO;
import tetish_inn_backend.tetish_inn.modules.user.dto.SaveUserDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tetish_inn_backend.tetish_inn.common.utils.GlobalCC.getCurrentUser;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> create(SaveUserDTO request) {
        User user = UserMapper.toEntity(request);
        boolean exists = userRepository.existsByUsrEmail(request.getEmail());
        boolean existsPhone = userRepository.existsByUsrPhone(request.getPhone());
        if (existsPhone) {
            return ResponseEntity.ok(ApiResponse.error(
                    "Phone number already in use"
            ));
        }
        if (exists) {
            return ResponseEntity.ok(ApiResponse.error(
                    "Email already in use"
            ));
        }
        user.setUsrPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsrBalance(new BigDecimal(1500));
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(
                "User created successfully",
                savedUser
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> userSummary(){
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> mainResult = new HashMap<>();
        List<OrderRequestDTO>list = new ArrayList<>();
        User user = getCurrentUser();

        result.put("userName", user.getUsrNames().split(" ")[0]);
        result.put("orders", new BigDecimal(0));
        result.put("balance", user.getUsrBalance());
        result.put("joinDate", user.getCreatedAt());
        result.put("recentOrder", new BigDecimal(0));
        result.put("orderHistory", list);

        mainResult.put("result", result);

        return ResponseEntity.ok().body(
                ApiResponse.success("Okay", mainResult)
        );
    }
}
