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
        entity.setMaxDurationDays(domain.getMaxDurationDays());
        entity.setMaxExtensions(domain.getMaxExtensions());
        entity.setPositionEffect(domain.efeitoNoLugar().name());
        entity.setVacancyAfterDays(domain.getVacancyAfterDays());
        entity.setReturnEffect(domain.efeitoNoRegresso().name());
        // A V6 declara name NOT NULL; sem isto, criar um subtipo pela API falha.
        entity.setName(domain.getDescription() != null ? domain.getDescription() : domain.getCode());
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
            entity.getIsActive() != null && entity.getIsActive(),
            entity.getMaxDurationDays(),
            entity.getMaxExtensions(),
            entity.getPositionEffect(),
            entity.getVacancyAfterDays(),
            entity.getReturnEffect()
        );
    }

    public LeaveMobilitySubtypeResponseDTO toDTO(LeaveMobilitySubtype domain) {
        if (domain == null) return null;
        var dto = new LeaveMobilitySubtypeResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setDescription(domain.getDescription());
        dto.setRecordType(domain.getRecordType());
        dto.setRecordTypeDesc(domain.getRecordType());
        dto.setAffectsPay(domain.isAffectsPay());
        dto.setCountsForSeniority(domain.isCountsForSeniority());
        dto.setCanSelfSubmit(domain.isCanSelfSubmit());
        dto.setMaxDurationDays(domain.getMaxDurationDays());
        dto.setMaxExtensions(domain.getMaxExtensions());
        dto.setPositionEffect(domain.efeitoNoLugar().name());
        dto.setVacancyAfterDays(domain.getVacancyAfterDays());
        dto.setReturnEffect(domain.efeitoNoRegresso().name());
        dto.setIsActive(domain.isActive());
        dto.setEstadoDesc(Boolean.TRUE.equals(domain.isActive()) ? "Ativo" : "Inativo");
        return dto;
    }
}
