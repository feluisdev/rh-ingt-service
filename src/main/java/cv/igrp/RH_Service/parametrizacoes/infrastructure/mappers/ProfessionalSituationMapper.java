package cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ProfessionalSituationResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ProfessionalSituation;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.ProfessionalSituationEntity;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ProfessionalSituationId;
import org.springframework.stereotype.Component;

@Component
public class ProfessionalSituationMapper {

    public ProfessionalSituationEntity toEntity(ProfessionalSituation domain) {
        if (domain == null) return null;
        ProfessionalSituationEntity entity = new ProfessionalSituationEntity();
        entity.setId(domain.getId().getValor());
        entity.setCode(domain.getCode());
        entity.setDescription(domain.getDescription());
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public ProfessionalSituation toDomain(ProfessionalSituationEntity entity) {
        if (entity == null) return null;
        return ProfessionalSituation.reconstruir(
            ProfessionalSituationId.from(entity.getId()),
            entity.getCode(),
            entity.getDescription(),
            entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public ProfessionalSituationResponseDTO toDTO(ProfessionalSituation domain) {
        if (domain == null) return null;
        var dto = new ProfessionalSituationResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setDescription(domain.getDescription());
        dto.setIsActive(domain.isActive());
        return dto;
    }
}
