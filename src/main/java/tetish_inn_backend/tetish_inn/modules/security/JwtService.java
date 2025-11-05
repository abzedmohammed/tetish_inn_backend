package tetish_inn_backend.tetish_inn.modules.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tetish_inn_backend.tetish_inn.modules.user.User;
import tetish_inn_backend.tetish_inn.modules.user.UserRepository;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    private final UserRepository userRepository;

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final int accessExpirationMinutes;
    private final int refreshExpirationDays;

    public JwtService(
            UserRepository userRepository, @Value("${app.jwt.access-secret}") String accessSecret,
            @Value("${app.jwt.refresh-secret}") String refreshSecret,
            @Value("${app.jwt.access-expiration-minutes}") int accessExpirationMinutes,
            @Value("${app.jwt.refresh-expiration-days}") int refreshExpirationDays
    ) {
        this.userRepository = userRepository;
        this.accessKey = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(refreshSecret));
        this.accessExpirationMinutes = accessExpirationMinutes;
        this.refreshExpirationDays = refreshExpirationDays;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getUsrEmail());
        claims.put("role", user.getUsrType());
        claims.put("name", user.getUsrNames());
        claims.put("avatar", user.getUsrAvatar());

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
            Jwts.parser().build().parse(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            Jwts.parser().build().parse(token);
            return true;
        } catch (JwtException ex) {
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

        String userId = claims.getSubject(); // you used user ID as subject

        return userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    public Date getRefreshExpiration(String token) {
        Claims claims = extractAllClaims(token, refreshKey);
        return claims.getExpiration();
    }

}

