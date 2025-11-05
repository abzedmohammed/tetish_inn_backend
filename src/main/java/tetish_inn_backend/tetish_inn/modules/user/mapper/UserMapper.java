package tetish_inn_backend.tetish_inn.modules.user.mapper;

import tetish_inn_backend.tetish_inn.modules.user.User;
import tetish_inn_backend.tetish_inn.modules.user.dto.SaveUserDTO;

public class UserMapper {
    public static User toEntity(SaveUserDTO dto){
        User entity = new User();
        entity.setUsrNames(dto.getNames());
        entity.setUsrEmail(dto.getEmail());
        entity.setUsrPhone(dto.getPhone());
        entity.setUsrPassword(dto.getPassword());
        entity.setUsrType(dto.getProfile());
        entity.setUsrAvatar(dto.getAvatar());

        return  entity;
    }
}
