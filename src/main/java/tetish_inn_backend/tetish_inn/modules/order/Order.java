package tetish_inn_backend.tetish_inn.modules.order;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tetish_inn_backend.tetish_inn.modules.audit.BaseEntity;
import tetish_inn_backend.tetish_inn.modules.snack.Snack;
import tetish_inn_backend.tetish_inn.modules.user.User;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID ordId;

    @NotNull
    private BigDecimal ordAmount;

    @NotNull
    private BigDecimal ordTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snack_id", nullable = false)
    private Snack snack;
}
