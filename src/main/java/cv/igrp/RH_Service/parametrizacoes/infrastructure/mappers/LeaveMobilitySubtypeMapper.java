package cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers;

import cv.igrp.RH_Service.parametrizacoes.application.dto.LeaveMobilitySubtypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveMobilitySubtype;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.LeaveMobilitySubtypeEntity;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveMobilitySubtypeId;
import org.springframework.stereotype.Component;

@Component
public class LeaveMobilitySubtypeMapper {

    public LeaveMobilitySubtypeEntity toEntity(LeaveMobilitySubtype domain) {
        if (domain == null) return null;
        LeaveMobilitySubtypeEntity entity = new LeaveMobilitySubtypeEntity();
        entity.setId(domain.getId().getValor());
        entity.setCode(domain.getCode());
        entity.setDescription(domain.getDescription());
        entity.setRecordType(domain.getRecordType());
        entity.setAffectsPay(domain.isAffectsPay());
        entity.setCountsForSeniority(domain.isCountsForSeniority());
        entity.setCanSelfSubmit(domain.isCanSelfSubmit());
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public LeaveMobilitySubtype toDomain(LeaveMobilitySubtypeEntity entity) {
        if (entity == null) return null;
        return LeaveMobilitySubtype.reconstruir(
            LeaveMobilitySubtypeId.from(entity.getId()),
            entity.getCode(),
            entity.getDescription(),
            entity.getRecordType(),
            entity.getAffectsPay() != null && entity.getAffectsPay(),
            entity.getCountsForSeniority() != null && entity.getCountsForSeniority(),
            entity.getCanSelfSubmit() != null && entity.getCanSelfSubmit(),
            entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public LeaveMobilitySubtypeResponseDTO toDTO(LeaveMobilitySubtype domain) {
        if (domain == null) return null;
        var dto = new LeaveMobilitySubtypeResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setDescription(domain.getDescription());
        dto.setRecordType(domain.getRecordType());
        dto.setAffectsPay(domain.isAffectsPay());
        dto.setCountsForSeniority(domain.isCountsForSeniority());
        dto.setCanSelfSubmit(domain.isCanSelfSubmit());
        dto.setIsActive(domain.isActive());
        return dto;
    }
}
