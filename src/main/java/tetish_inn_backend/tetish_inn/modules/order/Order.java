package tetish_inn_backend.tetish_inn.modules.order;

    import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;
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

    private UUID id;


    private BigDecimal ordAmount;

    private BigDecimal ordTotal;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)

    @JoinColumn(name = "snack_id")

    private Snack snack;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)

    @JoinColumn(name = "user_id")

    private User user;
    }
