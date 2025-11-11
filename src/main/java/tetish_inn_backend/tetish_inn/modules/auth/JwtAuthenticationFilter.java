package tetish_inn_backend.tetish_inn.modules.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tetish_inn_backend.tetish_inn.modules.security.JwtService;
import tetish_inn_backend.tetish_inn.modules.user.User;
import tetish_inn_backend.tetish_inn.modules.user.UserDetailsImpl;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws IOException, ServletException {

        final String authHeader = request.getHeader("Authorization");
        User user = null;
        String jwt = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);

            try {
                if (jwtService.validateAccessToken(jwt)) {
                    user = jwtService.extractUserFromAccessToken(jwt);
                }
            } catch (Exception ignored) {
            }
        }

        if (user != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetailsImpl userDetails = new UserDetailsImpl(user);
            var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        filterChain.doFilter(request, response);
    }
}
