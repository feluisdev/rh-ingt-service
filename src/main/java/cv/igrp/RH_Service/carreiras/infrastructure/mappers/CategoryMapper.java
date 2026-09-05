package cv.igrp.RH_Service.carreiras.infrastructure.mappers;

import cv.igrp.RH_Service.carreiras.application.dto.CategoryResponseDTO;
import cv.igrp.RH_Service.carreiras.domain.models.Category;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CareerId;
import cv.igrp.RH_Service.carreiras.domain.valueobject.CategoryId;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.CategoryEntity;
import cv.igrp.RH_Service.carreiras.infrastructure.persistence.entity.CareerEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryMapper {

    private final JpaReferences refs;

    public CategoryEntity toEntity(Category domain) {
        if (domain == null) return null;
        CategoryEntity entity = new CategoryEntity();
        entity.setId(domain.getId().getValor());
        entity.setCareer(refs.ref(CareerEntity.class, domain.getCareerId().getValor()));
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setOrdemProgressao(domain.getOrdemProgressao());
        entity.setIsActive(domain.getIsActive());
        return entity;
    }

    public Category toDomain(CategoryEntity entity) {
        if (entity == null) return null;
        return Category.reconstituir(
                CategoryId.from(entity.getId()),
                CareerId.from(entity.getCareer().getId()),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getOrdemProgressao(),
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
        dto.setOrdemProgressao(domain.getOrdemProgressao());
        dto.setIsActive(domain.getIsActive());
        dto.setEstadoDesc(Boolean.TRUE.equals(domain.getIsActive()) ? "Ativo" : "Inativo");
        return dto;
    }
}
