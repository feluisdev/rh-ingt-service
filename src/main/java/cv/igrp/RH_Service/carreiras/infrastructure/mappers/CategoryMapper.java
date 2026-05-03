package cv.igrp.RH_Service.carreiras.infrastructure.mappers;

import cv.igrp.RH_Service.carreiras.application.dto.CategoryResponseDTO;
import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryEntity toEntity(Category domain) {
        if (domain == null) return null;
        CategoryEntity entity = new CategoryEntity();
        entity.setId(domain.getId().getValor());
        entity.setCareerId(domain.getCareerId().getValor());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setIsActive(domain.getIsActive());
        return entity;
    }

    public Category toDomain(CategoryEntity entity) {
        if (entity == null) return null;
        return Category.reconstituir(
                CategoryId.from(entity.getId()),
                CareerId.from(entity.getCareerId()),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public CategoryResponseDTO toDTO(Category domain) {
        return toDTO(domain, null);
    }

    public CategoryResponseDTO toDTO(Category domain, String careerName) {
        if (domain == null) return null;
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCareerId(domain.getCareerId().getStringValor());
        dto.setCareerName(careerName);
        dto.setCode(domain.getCode());
        dto.setName(domain.getName());
        dto.setDescription(domain.getDescription());
        dto.setIsActive(domain.getIsActive());
        return dto;
    }
}
