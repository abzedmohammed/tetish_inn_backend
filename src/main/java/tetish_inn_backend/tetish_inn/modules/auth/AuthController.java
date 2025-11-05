package tetish_inn_backend.tetish_inn.modules.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    @PostMapping("/login")
    public ResponseEntity<AuthResponseBody> login(@RequestBody AuthRequest request) {
        AuthResponse auth = authService.login(request.getEmail(), request.getPassword());

        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, auth.refreshToken())
                .httpOnly(true)
                .secure(false) // set to true in production (HTTPS)
                .sameSite("Lax")
                .path("/auth/refresh")
                .maxAge(7 * 24 * 60 * 60)
                .build();

        AuthResponseBody body = new AuthResponseBody(auth.accessToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseBody> refresh(@CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String newAccess = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(new AuthResponseBody(newAccess));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken) {
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }

        ResponseCookie deleteCookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/auth/refresh")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body("Logged out");
    }
}

