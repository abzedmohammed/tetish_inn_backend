package tetish_inn_backend.tetish_inn.modules.auth;

import java.util.UUID;

public record AuthResponse(String accessToken, String refreshToken, UUID userId) {
}
