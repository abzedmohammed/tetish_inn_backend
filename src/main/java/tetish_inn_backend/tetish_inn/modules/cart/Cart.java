package tetish_inn_backend.tetish_inn.modules.cart;

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
    @Table(name = "carts")
    public class Cart extends BaseEntity {

    @Id

    @GeneratedValue(strategy = GenerationType.AUTO)

    private UUID id;


    private BigDecimal crtAmount;

    private Integer crtQuantity;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)

    @JoinColumn(name = "snack_id")

    private Snack snack;


    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "user_id")

    private User user;
    }
