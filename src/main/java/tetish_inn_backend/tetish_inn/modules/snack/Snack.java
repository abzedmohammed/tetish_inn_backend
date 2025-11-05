package tetish_inn_backend.tetish_inn.modules.snack;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import tetish_inn_backend.tetish_inn.common.enums.SnackType;
import tetish_inn_backend.tetish_inn.modules.audit.BaseEntity;
import tetish_inn_backend.tetish_inn.modules.user.User;

import java.math.BigDecimal;
import java.util.UUID;


@Getter
@Setter
@Entity
@Table(name = "snacks")
public class Snack extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID snkId;

    private String snkName;
    private BigDecimal snkPrice;
    private String snkImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snk_usr_id", referencedColumnName = "usrId")
    private User snkUsrId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String snkDescription;


    @Enumerated(EnumType.STRING)
    private SnackType snkType;

}