package cv.igrp.RH_Service.estrutura.infrastructure.mappers;

import cv.igrp.RH_Service.estrutura.application.dto.FunctionResponse;
import cv.igrp.RH_Service.estrutura.domain.models.OrgFunction;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.FunctionEntity;
import org.springframework.stereotype.Component;

@Component
public class FunctionMapper {

    public FunctionEntity toEntity(OrgFunction domain) {
        if (domain == null) return null;
        FunctionEntity entity = new FunctionEntity();
        entity.setId(domain.getId().getValor());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public OrgFunction toDomain(FunctionEntity entity) {
        if (entity == null) return null;
        return OrgFunction.reconstruir(
                FunctionId.from(entity.getId()),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public FunctionResponse toDTO(OrgFunction domain) {
        if (domain == null) return null;
        FunctionResponse dto = new FunctionResponse();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());
        dto.setIsActive(domain.isActive());
        return dto;
    }
}
