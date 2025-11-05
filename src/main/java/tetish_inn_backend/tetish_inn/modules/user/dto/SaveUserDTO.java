package tetish_inn_backend.tetish_inn.modules.user.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import tetish_inn_backend.tetish_inn.common.enums.UserTypes;

@Data
public class SaveUserDTO {
    private String names;
    private String email;
    private String phone;
    private String password;
    private String avatar;

    @Enumerated(EnumType.STRING)
    private UserTypes profile;
}
