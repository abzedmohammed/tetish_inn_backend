package tetish_inn_backend.tetish_inn.modules.cart;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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


    private BigDecimal crtTotal;

    private BigDecimal crtQuantity;


    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)

    private List<User> users = new ArrayList<>();


    @ManyToMany(fetch = FetchType.LAZY)

    @JoinTable(

            name = "cart_snack",

            joinColumns = @JoinColumn(name = "cart_id"),

            inverseJoinColumns = @JoinColumn(name = "snack_id")

    )

    private Set<Snack> snacks = new HashSet<>();
}
