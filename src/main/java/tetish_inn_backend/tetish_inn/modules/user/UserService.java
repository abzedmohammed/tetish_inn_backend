package tetish_inn_backend.tetish_inn.modules.user;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import tetish_inn_backend.tetish_inn.common.utils.ApiResponse;
import tetish_inn_backend.tetish_inn.modules.user.dto.SaveUserDTO;

@Service
@AllArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success(
                "User created successfully",
                savedUser
        ));
    }
}
