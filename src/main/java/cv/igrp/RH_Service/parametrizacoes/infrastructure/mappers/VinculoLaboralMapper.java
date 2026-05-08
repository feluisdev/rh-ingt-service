package cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers;

import cv.igrp.RH_Service.parametrizacoes.application.dto.VinculoLaboralResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VinculoLaboral;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.VinculoLaboralEntity;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.VinculoLaboralId;
import org.springframework.stereotype.Component;

@Component
public class VinculoLaboralMapper {

    public VinculoLaboralEntity toEntity(VinculoLaboral domain) {
        if (domain == null) return null;
        VinculoLaboralEntity entity = new VinculoLaboralEntity();
        entity.setId(domain.getId().getValor());
        entity.setCode(domain.getCode());
        entity.setDescription(domain.getDescription());
        entity.setCountsSeniority(domain.isCountsSeniority());
        entity.setEligibleForProgression(domain.isEligibleForProgression());
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public VinculoLaboral toDomain(VinculoLaboralEntity entity) {
        if (entity == null) return null;
        return VinculoLaboral.reconstruir(
            VinculoLaboralId.from(entity.getId()),
            entity.getCode(),
            entity.getDescription(),
            entity.getCountsSeniority() != null && entity.getCountsSeniority(),
            entity.getEligibleForProgression() != null && entity.getEligibleForProgression(),
            entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public VinculoLaboralResponseDTO toDTO(VinculoLaboral domain) {
        if (domain == null) return null;
        var dto = new VinculoLaboralResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setDescription(domain.getDescription());
        dto.setCountsSeniority(domain.isCountsSeniority());
        dto.setEligibleForProgression(domain.isEligibleForProgression());
        dto.setIsActive(domain.isActive());
        return dto;
    }
}
