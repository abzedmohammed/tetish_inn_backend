package tetish_inn_backend.tetish_inn.modules.snack.mapper;

import tetish_inn_backend.tetish_inn.modules.snack.Snack;
import tetish_inn_backend.tetish_inn.modules.snack.dto.SnackRequestDTO;
import tetish_inn_backend.tetish_inn.modules.snack.dto.SnackResponseDTO;

public class SnackMapper {
    public static Snack toEntity(SnackRequestDTO dto) {
        Snack entity = new Snack();
        entity.setSnkName(dto.getSnkName());
        entity.setSnkPrice(dto.getSnkPrice());
        entity.setSnkImageUrl(dto.getSnkImageUrl());
        entity.setSnkType(dto.getSnkType());
        entity.setSnkDescription(dto.getSnkDescription());
        return entity;
    }

    public static SnackResponseDTO toDTO(Snack snack) {
        SnackResponseDTO dto = new SnackResponseDTO();
        dto.setSnkId(snack.getSnkId());
        dto.setSnkName(snack.getSnkName());
        dto.setSnkPrice(snack.getSnkPrice());
        dto.setSnkImageUrl(snack.getSnkImageUrl());
        dto.setSnkDescription(snack.getSnkDescription());
        dto.setSnkType(snack.getSnkType());

        dto.setCreatedAt(snack.getCreatedAt());
        dto.setUpdatedAt(snack.getUpdatedAt());
        dto.setCreatedBy(snack.getCreatedBy());
        dto.setUpdatedBy(snack.getUpdatedBy());
        return dto;
    }
}
