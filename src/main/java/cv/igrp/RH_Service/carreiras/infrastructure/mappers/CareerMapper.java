package cv.igrp.RH_Service.carreiras.infrastructure.mappers;

import cv.igrp.RH_Service.carreiras.application.dto.CareerResponse;
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
                entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public CareerResponse toDTO(Career domain) {
        if (domain == null) return null;
        CareerResponse dto = new CareerResponse();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());
        dto.setIsActive(domain.getIsActive());
        return dto;
    }
}
