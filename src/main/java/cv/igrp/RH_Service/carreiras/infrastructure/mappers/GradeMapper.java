package cv.igrp.RH_Service.carreiras.infrastructure.mappers;

import cv.igrp.RH_Service.carreiras.application.dto.GradeResponse;
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
                entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public GradeResponse toDTO(Grade domain) {
        return toDTO(domain, null);
    }

    public GradeResponse toDTO(Grade domain, String categoryName) {
        if (domain == null) return null;
        GradeResponse dto = new GradeResponse();
        dto.setId(domain.getId().getStringValor());
        dto.setCategoryId(domain.getCategoryId().getStringValor());
        dto.setCategoryName(categoryName);
        dto.setGradeNumber(domain.getGradeNumber());
        dto.setName(domain.getName());
        dto.setSalaryIndex(domain.getSalaryIndex());
        dto.setIsActive(domain.getIsActive());
        return dto;
    }
}
