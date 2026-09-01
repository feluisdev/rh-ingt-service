package cv.igrp.RH_Service.estrutura.infrastructure.mappers;

import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.estrutura.application.dto.PositionResponseDTO;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.JobRepository;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.JobId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.PositionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PositionMapper {

    private final JobRepository jobRepository;
    private final OrganizationalUnitRepository unidadeRepository;
    private final CareerRepository careerRepository;
    private final CategoryRepository categoryRepository;

    public PositionResponseDTO toDTO(Position domain) {
        if (domain == null) return null;
        PositionResponseDTO dto = new PositionResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setNumeroLugar(domain.getNumeroLugar());
        dto.setJobId(str(domain.getJobId()));
        dto.setJobNome(jobNome(domain.getJobId()));
        dto.setUnidadeOrganicaId(str(domain.getUnidadeOrganicaId()));
        dto.setUnidadeNome(unidadeNome(domain.getUnidadeOrganicaId()));
        dto.setCareerId(str(domain.getCareerId()));
        dto.setCareerNome(careerNome(domain.getCareerId()));
        dto.setCategoryId(str(domain.getCategoryId()));
        dto.setCategoryNome(categoryNome(domain.getCategoryId()));
        dto.setParentPositionId(str(domain.getParentPositionId()));
        dto.setManagesUnitId(str(domain.getManagesUnitId()));
        dto.setEstado(domain.getEstado());
        dto.setLegalBase(domain.getLegalBase());
        dto.setIsActive(domain.isActive());
        dto.setForaDeGrelha(domain.isForaDeGrelha());
        return dto;
    }

    private String jobNome(UUID id) {
        return id == null ? null : jobRepository.findById(JobId.from(id))
                .map(cv.igrp.RH_Service.estrutura.domain.models.Job::getName).orElse(null);
    }

    private String unidadeNome(UUID id) {
        return id == null ? null : unidadeRepository.findById(OrganizationalUnitId.from(id))
                .map(cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit::getName).orElse(null);
    }

    private String careerNome(UUID id) {
        return id == null ? null : careerRepository.findById(CareerId.from(id))
                .map(cv.igrp.RH_Service.carreiras.domain.models.Career::getName).orElse(null);
    }

    private String categoryNome(UUID id) {
        return id == null ? null : categoryRepository.findById(CategoryId.from(id))
                .map(cv.igrp.RH_Service.carreiras.domain.models.Category::getName).orElse(null);
    }

    private static String str(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    public PositionEntity toEntity(Position domain) {
        if (domain == null) return null;
        PositionEntity entity = new PositionEntity();
        entity.setId(domain.getId().getValor());
        entity.setNumeroLugar(domain.getNumeroLugar());
        entity.setJobId(domain.getJobId());
        entity.setUnidadeOrganicaId(domain.getUnidadeOrganicaId());
        entity.setCareerId(domain.getCareerId());
        entity.setCategoryId(domain.getCategoryId());
        entity.setParentPositionId(domain.getParentPositionId());
        entity.setManagesUnitId(domain.getManagesUnitId());
        entity.setEstado(domain.getEstado());
        entity.setLegalBase(domain.getLegalBase());
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public Position toDomain(PositionEntity entity) {
        if (entity == null) return null;
        return Position.reconstituir(
                PositionId.from(entity.getId()),
                entity.getNumeroLugar(),
                entity.getJobId(),
                entity.getUnidadeOrganicaId(),
                entity.getCareerId(),
                entity.getCategoryId(),
                entity.getParentPositionId(),
                entity.getManagesUnitId(),
                entity.getEstado(),
                entity.getLegalBase(),
                entity.getIsActive() != null && entity.getIsActive()
        );
    }
}
