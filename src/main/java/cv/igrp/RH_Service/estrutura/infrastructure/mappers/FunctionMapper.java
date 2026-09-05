package cv.igrp.RH_Service.estrutura.infrastructure.mappers;

import cv.igrp.RH_Service.estrutura.application.dto.FunctionResponseDTO;
import cv.igrp.RH_Service.estrutura.domain.models.OrgFunction;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.FunctionEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.JobEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FunctionMapper {

    private final JpaReferences refs;

    private final JobRepository jobRepository;

    public FunctionEntity toEntity(OrgFunction domain) {
        if (domain == null) return null;
        FunctionEntity entity = new FunctionEntity();
        entity.setId(domain.getId().getValor());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setJob(refs.ref(JobEntity.class, domain.getJobId()));
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
                refs.idOf(entity.getJob(), JobEntity::getId),
                entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public FunctionResponseDTO toDTO(OrgFunction domain) {
        if (domain == null) return null;
        FunctionResponseDTO dto = new FunctionResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());
        dto.setJobId(domain.getJobId() != null ? domain.getJobId().toString() : null);
        if (domain.getJobId() != null) {
            jobRepository.findById(JobId.from(domain.getJobId())).ifPresent(job -> {
                dto.setJobName(job.getName());
                dto.setJobDescription(job.getDescription());
            });
        }
        dto.setIsActive(domain.isActive());
        dto.setEstadoDesc(Boolean.TRUE.equals(domain.isActive()) ? "Ativo" : "Inativo");
        return dto;
    }
}
