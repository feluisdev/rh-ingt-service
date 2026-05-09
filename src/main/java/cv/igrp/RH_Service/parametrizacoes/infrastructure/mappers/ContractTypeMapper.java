package cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ContractTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContractType;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.ContractTypeEntity;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import org.springframework.stereotype.Component;

@Component
public class ContractTypeMapper {

    public ContractTypeEntity toEntity(ContractType domain) {
        if (domain == null) return null;
        ContractTypeEntity entity = new ContractTypeEntity();
        entity.setId(domain.getId().getValor());
        entity.setCode(domain.getCode());
        entity.setDescription(domain.getDescription());
        entity.setVinculoLaboralId(domain.getVinculoLaboralId());
        entity.setIsRenewable(domain.isRenewable());
        entity.setMaxRenewals(domain.getMaxRenewals());
        entity.setMaxDurationMonths(domain.getMaxDurationMonths());
        entity.setRequiresCareerStructure(domain.isRequiresCareerStructure());
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public ContractType toDomain(ContractTypeEntity entity) {
        if (entity == null) return null;
        return ContractType.reconstruir(
            ContractTypeId.from(entity.getId()),
            entity.getCode(),
            entity.getDescription(),
            entity.getVinculoLaboralId(),
            Boolean.TRUE.equals(entity.getIsRenewable()),
            entity.getMaxRenewals(),
            entity.getMaxDurationMonths(),
            Boolean.TRUE.equals(entity.getRequiresCareerStructure()),
            entity.getIsActive() != null && entity.getIsActive()
        );
    }

    public ContractTypeResponseDTO toDTO(ContractType domain) {
        if (domain == null) return null;
        var dto = new ContractTypeResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setDescription(domain.getDescription());
        dto.setVinculoLaboralId(domain.getVinculoLaboralId() != null
                ? domain.getVinculoLaboralId().toString() : null);
        dto.setIsRenewable(domain.isRenewable());
        dto.setMaxRenewals(domain.getMaxRenewals());
        dto.setMaxDurationMonths(domain.getMaxDurationMonths());
        dto.setRequiresCareerStructure(domain.isRequiresCareerStructure());
        dto.setIsActive(domain.isActive());
        return dto;
    }
}
