package tetish_inn_backend.tetish_inn.modules.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tetish_inn_backend.tetish_inn.modules.user.User;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    public void createRefreshToken(User user, String token, Instant expiry) {
        RefreshToken t = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiry)
                .revoked(false)
                .build();
        repository.save(t);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return repository.findByToken(token);
    }

    public void revoke(RefreshToken token) {
        token.setRevoked(true);
        repository.save(token);
    }

    public void revokeAllForUser(UUID userId) {
        repository.deleteByUserUsrId(userId);
    }
}

