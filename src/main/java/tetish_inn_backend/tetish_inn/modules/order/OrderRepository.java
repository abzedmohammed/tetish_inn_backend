package tetish_inn_backend.tetish_inn.modules.order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
