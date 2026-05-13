package cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers;

import cv.igrp.RH_Service.parametrizacoes.application.dto.PublicHolidayResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PublicHoliday;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.PublicHolidayEntity;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.PublicHolidayId;
import org.springframework.stereotype.Component;

@Component
public class PublicHolidayMapper {

    public PublicHolidayEntity toEntity(PublicHoliday domain) {
        if (domain == null) return null;
        PublicHolidayEntity entity = new PublicHolidayEntity();
        entity.setId(domain.getId().getValor());
        entity.setName(domain.getName());
        entity.setHolidayDate(domain.getHolidayDate());
        entity.setIsNational(domain.isNational());
        entity.setDescription(domain.getDescription());
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public PublicHoliday toDomain(PublicHolidayEntity entity) {
        if (entity == null) return null;
        return PublicHoliday.reconstruir(
            PublicHolidayId.from(entity.getId()),
            entity.getName(),
            entity.getHolidayDate(),
            entity.getIsNational() != null && entity.getIsNational(),
            entity.getDescription(),
            entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public PublicHolidayResponseDTO toDTO(PublicHoliday domain) {
        if (domain == null) return null;
        var dto = new PublicHolidayResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setName(domain.getName());
        dto.setHolidayDate(domain.getHolidayDate().toString());
        dto.setIsNational(domain.isNational());
        dto.setDescription(domain.getDescription());
        dto.setIsActive(domain.isActive());
        dto.setEstadoDesc(Boolean.TRUE.equals(domain.isActive()) ? "Ativo" : "Inativo");
        return dto;
    }
}
