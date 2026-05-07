package cv.igrp.RH_Service.carreiras.infrastructure.mappers;

import cv.igrp.RH_Service.carreiras.application.dto.GradeResponseDTO;
import cv.igrp.RH_Service.carreiras.domain.models.Grade;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.GradeId;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.GradeEntity;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {

    public GradeEntity toEntity(Grade domain) {
        if (domain == null) return null;
        GradeEntity entity = new GradeEntity();
        entity.setId(domain.getId().getValor());
        entity.setCategoryId(domain.getCategoryId().getValor());
        entity.setGradeNumber(domain.getGradeNumber());
        entity.setName(domain.getName());
        entity.setSalaryIndex(domain.getSalaryIndex());
        entity.setSalaryBase(domain.getSalaryBase());
        entity.setIsActive(domain.getIsActive());
        return entity;
    }

    public Grade toDomain(GradeEntity entity) {
        if (entity == null) return null;
        return Grade.reconstituir(
                GradeId.from(entity.getId()),
                CategoryId.from(entity.getCategoryId()),
                entity.getGradeNumber(),
                entity.getName(),
                entity.getSalaryIndex(),
                entity.getSalaryBase(),
                entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public GradeResponseDTO toDTO(Grade domain) {
        return toDTO(domain, null);
    }

    public GradeResponseDTO toDTO(Grade domain, String categoryName) {
        if (domain == null) return null;
        GradeResponseDTO dto = new GradeResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCategoryId(domain.getCategoryId().getStringValor());
        dto.setCategoryName(categoryName);
        dto.setGradeNumber(domain.getGradeNumber());
        dto.setName(domain.getName());
        dto.setSalaryIndex(domain.getSalaryIndex());
        dto.setSalaryBase(domain.getSalaryBase());
        dto.setIsActive(domain.getIsActive());
        dto.setEstadoDesc(Boolean.TRUE.equals(domain.getIsActive()) ? "Ativo" : "Inativo");
        return dto;
    }
}
