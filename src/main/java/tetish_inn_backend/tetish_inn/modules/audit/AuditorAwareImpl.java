package tetish_inn_backend.tetish_inn.modules.audit;

import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tetish_inn_backend.tetish_inn.modules.user.User;

import java.util.Optional;
import java.util.UUID;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<UUID> {
    @Override
    @NonNull
    public Optional<UUID> getCurrentAuditor() {
        // TODO: Get currently authenticated user instead of this placeholder
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof User user) {
                    return Optional.of(user.getUsrId());
                }
            }
        } catch (Exception e) {
            return Optional.empty();
        }
        return Optional.empty();
//        return Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }
}
