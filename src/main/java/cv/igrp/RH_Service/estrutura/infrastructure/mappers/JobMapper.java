package cv.igrp.RH_Service.estrutura.infrastructure.mappers;

import cv.igrp.RH_Service.estrutura.application.dto.JobResponseDTO;
import cv.igrp.RH_Service.estrutura.domain.models.Job;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.JobEntity;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public JobEntity toEntity(Job domain) {
        if (domain == null) return null;
        JobEntity entity = new JobEntity();
        entity.setId(domain.getId().getValor());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public Job toDomain(JobEntity entity) {
        if (entity == null) return null;
        return Job.reconstruir(
                JobId.from(entity.getId()),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public JobResponseDTO toDTO(Job domain) {
        if (domain == null) return null;
        JobResponseDTO dto = new JobResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());
        dto.setIsActive(domain.isActive());
        return dto;
    }
}
