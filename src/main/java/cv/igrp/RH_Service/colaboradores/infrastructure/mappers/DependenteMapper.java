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
                e.getNome(), e.getParentesco(), e.getDataNascimento(), e.getNif(), e.getIsActive());
    }

    public DependenteEntity toEntity(Dependente d) {
        DependenteEntity e = new DependenteEntity();
        e.setId(d.getId().getValor());
        e.setFuncionarioId(d.getFuncionarioId().getValor());
        e.setNome(d.getNome());
        e.setParentesco(d.getParentesco());
        e.setDataNascimento(d.getDataNascimento());
        e.setNif(d.getNif());
        e.setIsActive(d.getIsActive());
        return e;
    }

    public DependenteResponseDTO toDTO(Dependente d) {
        DependenteResponseDTO r = new DependenteResponseDTO();
        r.setId(d.getId().getStringValor());
        r.setFuncionarioId(d.getFuncionarioId().getStringValor());
        r.setNome(d.getNome());
        r.setParentesco(d.getParentesco());
        r.setDataNascimento(d.getDataNascimento());
        r.setNif(d.getNif());
        r.setIsActive(d.getIsActive());
        return r;
    }
}
