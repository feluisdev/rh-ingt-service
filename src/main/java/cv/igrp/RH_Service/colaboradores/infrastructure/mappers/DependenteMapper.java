package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.DependenteResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Dependente;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DependenteId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DependenteEntity;
import org.springframework.stereotype.Component;

@Component("colabsDependenteMapper")
public class DependenteMapper {

    public Dependente toDomain(DependenteEntity e) {
        return Dependente.reconstituir(
                DependenteId.from(e.getId()),
                FuncionarioId.from(e.getFuncionarioId()),
                e.getFullName(), e.getRelationshipType(),
                e.getBirthDate(), e.getNif(), e.getIsActive());
    }

    public DependenteEntity toEntity(Dependente d) {
        DependenteEntity e = new DependenteEntity();
        e.setId(d.getId().getValor());
        e.setFuncionarioId(d.getFuncionarioId().getValor());
        e.setFullName(d.getFullName());
        e.setRelationshipType(d.getRelationshipType());
        e.setBirthDate(d.getBirthDate());
        e.setNif(d.getNif());
        e.setIsActive(d.getIsActive());
        return e;
    }

    public DependenteResponseDTO toDTO(Dependente d) {
        DependenteResponseDTO r = new DependenteResponseDTO();
        r.setId(d.getId().getStringValor());
        r.setFuncionarioId(d.getFuncionarioId().getStringValor());
        r.setFullName(d.getFullName());
        r.setRelationshipType(d.getRelationshipType());
        r.setBirthDate(d.getBirthDate());
        r.setNif(d.getNif());
        r.setIsActive(d.getIsActive());
        r.setEstadoDesc(Boolean.TRUE.equals(d.getIsActive()) ? "Ativo" : "Inativo");
        return r;
    }
}
