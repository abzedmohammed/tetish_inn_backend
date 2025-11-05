package tetish_inn_backend.tetish_inn.modules.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    boolean existsByUsrEmail(String email);
    User findByUsrId(UUID id);
    Optional<User> findByUsrEmail(String email);
}
