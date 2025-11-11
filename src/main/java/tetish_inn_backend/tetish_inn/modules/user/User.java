package tetish_inn_backend.tetish_inn.modules.user;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;
import tetish_inn_backend.tetish_inn.common.enums.UserTypes;
import tetish_inn_backend.tetish_inn.modules.audit.BaseEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID usrId;

    private String usrNames;
    private String usrEmail;
    private String usrPhone;
    private String usrAvatar;
    private String usrAddress;
    private String usrPassword;
    private BigDecimal usrBalance;

    @Enumerated(EnumType.STRING)
    private UserTypes usrType;
}
