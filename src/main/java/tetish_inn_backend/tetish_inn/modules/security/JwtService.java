package tetish_inn_backend.tetish_inn.modules.security;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tetish_inn_backend.tetish_inn.modules.user.User;
import tetish_inn_backend.tetish_inn.modules.user.UserRepository;

import javax.crypto.SecretKey;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final UserRepository userRepository;
    private final Dotenv dotenv = Dotenv.load();

    String accessSecret = dotenv.get("APP_JWT_ACCESS_SECRET");
    String refreshSecret = dotenv.get("APP_JWT_REFRESH_SECRET");

    private final SecretKey accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
    private final SecretKey refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
    private final int accessExpirationMinutes = Integer.parseInt(dotenv.get("APP_JWT_ACCESS_EXPIRATION_MINUTES", "15"));
    private final int refreshExpirationDays = Integer.parseInt(dotenv.get("APP_JWT_REFRESH_EXPIRATION_DAYS", "1"));


    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getUsrEmail());
        claims.put("role", user.getUsrType());
        claims.put("name", user.getUsrNames());
        claims.put("avatar", user.getUsrAvatar());
        claims.put("balance", user.getUsrBalance());

        return Jwts.builder()
                .subject(user.getUsrId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessExpirationMinutes, ChronoUnit.MINUTES)))
                .claims(claims)
                .signWith(accessKey)
                .compact();
    }


    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshExpirationDays, ChronoUnit.DAYS)))
                .signWith(refreshKey)
                .compact();
    }


    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(refreshKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            System.out.println("Token has expired: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token has expired");
        } catch (JwtException ex) {
            System.out.println("Error validating token: " + ex.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException ex) {
            System.out.println("Error validating refresh token: " + ex.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


    public User extractUserFromAccessToken(String token) {
        Claims claims = extractAllClaims(token, accessKey);

        String userId = claims.getSubject();

        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    public User extractUserFromRefreshToken(String token) {
        Claims claims = extractAllClaims(token, refreshKey);

        String userId = claims.getSubject();

        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    public Date getRefreshExpiration(String token) {
        Claims claims = extractAllClaims(token, refreshKey);
        return claims.getExpiration();
    }

}

