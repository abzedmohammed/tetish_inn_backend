package tetish_inn_backend.tetish_inn.modules.snack;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SnackRepository extends JpaRepository<Snack, UUID> {
}
