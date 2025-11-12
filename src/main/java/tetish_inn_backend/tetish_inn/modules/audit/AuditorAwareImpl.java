package tetish_inn_backend.tetish_inn.modules.audit;

import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tetish_inn_backend.tetish_inn.modules.user.User;

import java.util.Optional;
import java.util.UUID;

import static tetish_inn_backend.tetish_inn.common.utils.GlobalCC.getCurrentUser;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<UUID> {
    @Override
    public Optional<UUID> getCurrentAuditor() {
        try {
            // safely wrap to handle nulls
            return Optional.of(getCurrentUser())
                    .map(User::getUsrId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
