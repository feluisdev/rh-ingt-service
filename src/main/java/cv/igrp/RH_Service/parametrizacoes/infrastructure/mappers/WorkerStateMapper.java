package cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WorkerStateResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.WorkerStateEntity;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import org.springframework.stereotype.Component;

@Component
public class WorkerStateMapper {

    public WorkerStateEntity toEntity(WorkerState domain) {
        if (domain == null) return null;
        WorkerStateEntity entity = new WorkerStateEntity();
        entity.setId(domain.getId().getValor());
        entity.setCode(domain.getCode());
        entity.setDescription(domain.getDescription());
        entity.setIsCore(domain.isCore());
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public WorkerState toDomain(WorkerStateEntity entity) {
        if (entity == null) return null;
        return WorkerState.reconstruir(
            WorkerStateId.from(entity.getId()),
            entity.getCode(),
            entity.getDescription(),
            entity.getIsCore() != null && entity.getIsCore(),
            entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public WorkerStateResponseDTO toDTO(WorkerState domain) {
        if (domain == null) return null;
        var dto = new WorkerStateResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setDescription(domain.getDescription());
        dto.setIsCore(domain.isCore());
        dto.setIsActive(domain.isActive());
        dto.setEstadoDesc(Boolean.TRUE.equals(domain.isActive()) ? "Ativo" : "Inativo");
        return dto;
    }
}
