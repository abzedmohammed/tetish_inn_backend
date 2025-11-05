package tetish_inn_backend.tetish_inn.modules.user.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResponseDTO {
    private UUID usrId;

    private String usrNames;
    private String usrEmail;
    private String usrPhone;
    private String usrAvatar;
}
