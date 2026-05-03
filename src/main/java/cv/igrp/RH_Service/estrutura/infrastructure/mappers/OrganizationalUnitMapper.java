package cv.igrp.RH_Service.estrutura.infrastructure.mappers;

import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitResponseDTO;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.OrganizationalUnitEntity;
import org.springframework.stereotype.Component;

@Component
public class OrganizationalUnitMapper {

    public OrganizationalUnitEntity toEntity(OrganizationalUnit domain) {
        if (domain == null) return null;
        OrganizationalUnitEntity entity = new OrganizationalUnitEntity();
        entity.setId(domain.getId().getValor());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setAcronym(domain.getAcronym());
        entity.setUnitTypeOptionId(domain.getUnitTypeOptionId());
        entity.setParentUnitId(domain.getParentUnitId() != null ? domain.getParentUnitId().getValor() : null);
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public OrganizationalUnit toDomain(OrganizationalUnitEntity entity) {
        if (entity == null) return null;
        OrganizationalUnitId parentId = entity.getParentUnitId() != null
                ? OrganizationalUnitId.from(entity.getParentUnitId()) : null;
        return OrganizationalUnit.reconstruir(
                OrganizationalUnitId.from(entity.getId()),
                entity.getCode(),
                entity.getName(),
                entity.getAcronym(),
                entity.getUnitTypeOptionId(),
                parentId,
                entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public OrganizationalUnitResponseDTO toDTO(OrganizationalUnit domain) {
        if (domain == null) return null;
        OrganizationalUnitResponseDTO dto = new OrganizationalUnitResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setName(domain.getName());
        dto.setAcronym(domain.getAcronym());
        dto.setUnitTypeOptionId(domain.getUnitTypeOptionId());
        dto.setParentUnitId(domain.getParentUnitId() != null ? domain.getParentUnitId().getValor() : null);
        dto.setIsActive(domain.isActive());
        return dto;
    }
}
