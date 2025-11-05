package tetish_inn_backend.tetish_inn.modules.audit;

import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("auditorAware")
public class AuditorAwareImpl implements AuditorAware<UUID> {
    @Override
    @NonNull
    public Optional<UUID> getCurrentAuditor() {
        // TODO: Get currently authenticated user instead of this placeholder
        return Optional.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    }
}
