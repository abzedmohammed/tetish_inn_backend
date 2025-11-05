package tetish_inn_backend.tetish_inn.modules.auth;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtil {

    public static ResponseCookie createRefreshCookie(String token, long maxAgeSeconds, boolean secure) {
        return ResponseCookie.from("refreshToken", token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/auth/refresh")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }

    public static ResponseCookie deleteRefreshCookie(boolean secure) {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/auth/refresh")
                .maxAge(0)
                .build();
    }
}

