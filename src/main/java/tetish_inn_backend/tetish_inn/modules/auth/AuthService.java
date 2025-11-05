package tetish_inn_backend.tetish_inn.modules.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tetish_inn_backend.tetish_inn.modules.security.JwtService;
import tetish_inn_backend.tetish_inn.modules.user.User;
import tetish_inn_backend.tetish_inn.modules.user.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse login(String email, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        User user = userRepository.findByUsrEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(email);

        Instant expiry = jwtService.getRefreshExpiration(refreshToken).toInstant();
        refreshTokenService.createRefreshToken(user, refreshToken, expiry);

        return new AuthResponse(accessToken, refreshToken, user.getUsrId());
    }

    public String refreshAccessToken(String refreshToken) {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        RefreshToken dbToken = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (dbToken.isRevoked() || dbToken.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token revoked or expired");
        }

        return jwtService.generateAccessToken(jwtService.extractUserFromRefreshToken(refreshToken));
    }

    public void logout(String refreshToken) {
        refreshTokenService.findByToken(refreshToken).ifPresent(refreshTokenService::revoke);
    }
}

