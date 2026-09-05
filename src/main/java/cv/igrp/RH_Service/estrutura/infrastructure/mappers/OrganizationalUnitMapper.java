package cv.igrp.RH_Service.estrutura.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitResponseDTO;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.OrganizationalUnitEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganizationalUnitMapper {

    private final JpaReferences refs;

    public OrganizationalUnitEntity toEntity(OrganizationalUnit domain) {
        if (domain == null) return null;
        OrganizationalUnitEntity entity = new OrganizationalUnitEntity();
        entity.setId(domain.getId().getValor());
        entity.setCode(domain.getCode());
        entity.setName(domain.getName());
        entity.setAcronym(domain.getAcronym());
        entity.setUnitType(domain.getUnitType());
        entity.setDescricao(domain.getDescricao());
        entity.setParentUnit(refs.ref(OrganizationalUnitEntity.class, domain.getParentUnitId() != null ? domain.getParentUnitId().getValor() : null));
        entity.setResponsibleEmployee(refs.ref(FuncionarioEntity.class, domain.getResponsibleEmployeeId()));
        entity.setIsActive(domain.isActive());
        return entity;
    }

    public OrganizationalUnit toDomain(OrganizationalUnitEntity entity) {
        if (entity == null) return null;
        OrganizationalUnitId parentId = refs.idOf(entity.getParentUnit(), OrganizationalUnitEntity::getId) != null
                ? OrganizationalUnitId.from(refs.idOf(entity.getParentUnit(), OrganizationalUnitEntity::getId)) : null;
        return OrganizationalUnit.reconstruir(
                OrganizationalUnitId.from(entity.getId()),
                entity.getCode(),
                entity.getName(),
                entity.getAcronym(),
                entity.getUnitType(),
                entity.getDescricao(),
                parentId,
                refs.idOf(entity.getResponsibleEmployee(), FuncionarioEntity::getId),
                entity.getIsActive() != null && entity.getIsActive()
        );
    }

    // Nao preenche o nome do responsavel aqui de proposito: o mapper nao tem
    // acesso ao modulo que o resolveria, e e isso que mantem a fronteira de
    // modulo. O nome resolve-se nos handlers de leitura, atras do port dedicado.
    public OrganizationalUnitResponseDTO toDTO(OrganizationalUnit domain) {
        if (domain == null) return null;
        OrganizationalUnitResponseDTO dto = new OrganizationalUnitResponseDTO();
        dto.setId(domain.getId().getStringValor());
        dto.setCode(domain.getCode());
        dto.setName(domain.getName());
        dto.setAcronym(domain.getAcronym());
        dto.setUnitType(domain.getUnitType());
        dto.setDescricao(domain.getDescricao());
        dto.setParentUnitId(domain.getParentUnitId() != null ? domain.getParentUnitId().getValor() : null);
        dto.setResponsibleEmployeeId(domain.getResponsibleEmployeeId());
        dto.setIsActive(domain.isActive());
        dto.setEstadoDesc(domain.isActive() ? "Ativo" : "Inativo");
        return dto;
    }
}
