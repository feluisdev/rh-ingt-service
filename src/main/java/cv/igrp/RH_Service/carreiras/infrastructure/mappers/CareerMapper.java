package cv.igrp.RH_Service.carreiras.infrastructure.mappers;

import cv.igrp.RH_Service.carreiras.application.dto.CareerResponseDTO;
import cv.igrp.RH_Service.carreiras.domain.models.Career;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.CareerEntity;
import org.springframework.stereotype.Component;

@Component
public class CareerMapper {

    public CareerEntity toEntity(Career domain) {
        if (domain == null) return null;
        CareerEntity entity = new CareerEntity();
        entity.setId(domain.getId().getValor());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setRegime(domain.getRegime());
        entity.setIsActive(domain.getIsActive());
        return entity;
    }

    public Career toDomain(CareerEntity entity) {
        if (entity == null) return null;
        return Career.reconstituir(
                CareerId.from(entity.getId()),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getRegime(),
                entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public CareerResponseDTO toDTO(Career domain) {
        if (domain == null) return null;
        CareerResponseDTO dto = new CareerResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());
        dto.setRegime(domain.getRegime());
        dto.setIsActive(domain.getIsActive());
        dto.setEstadoDesc(Boolean.TRUE.equals(domain.getIsActive()) ? "Ativo" : "Inativo");
        return dto;
    }
}
