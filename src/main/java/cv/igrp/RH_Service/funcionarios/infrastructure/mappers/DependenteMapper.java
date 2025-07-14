package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.DependenteResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Dependente;
import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DependenteEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FuncionarioEntity;
import org.springframework.stereotype.Component;

@Component
public class DependenteMapper {


  public Dependente toDomainWithFuncionario(DependenteEntity entity, Funcionario funcionarioDomain) {
    if (entity == null) {
      return null;
    }

    return Dependente.reconstruir(
        entity.getId(),
        ExternalID.from(entity.getExternalId()),
        entity.getNome(),
        entity.getDataNascimento(),
        entity.getParentesco(),
        entity.getCpf(),
        entity.getEstado(),
        funcionarioDomain
    );
  }

  public Dependente toDomain(DependenteEntity entity) {
    if (entity == null) {
      return null;
    }

    return Dependente.reconstruir(
        entity.getId(),
        ExternalID.from(entity.getExternalId()),
        entity.getNome(),
        entity.getDataNascimento(),
        entity.getParentesco(),
        entity.getCpf(),
        entity.getEstado(),
        entity.getId()
    );
  }

  public DependenteEntity toEntity(Dependente domain, FuncionarioEntity funcionarioEntity) {
    if (domain == null) {
      return null;
    }

    DependenteEntity entity = new DependenteEntity();
    if (domain.getId() != null) {
      entity.setId(domain.getId());
    }
    entity.setExternalId(domain.getExternalId().getValor());
    entity.setNome(domain.getNome());
    entity.setDataNascimento(domain.getDataNascimento());
    entity.setParentesco(domain.getParentesco());
    entity.setCpf(domain.getCpf());
    entity.setEstado(domain.getEstado());
    entity.setIdFuncionario(funcionarioEntity);

    return entity;
  }

  public DependenteResponseDTO toResponseDTO(Dependente dependente) {
    if (dependente == null) return null;

    DependenteResponseDTO dto = new DependenteResponseDTO();
    dto.setDependenteId(dependente.getExternalId().getStringValor());
    //dto.setFuncionarioId(dependente.getFuncionario().getExternalId().getStringValor());
    dto.setFuncionarioId(dependente.getFuncionarioExternalId() != null ? dependente.getFuncionarioExternalId().getStringValor() : null);
    dto.setNome(dependente.getNome());
    dto.setParentesco(dependente.getParentesco());
    dto.setDataNascimento(dependente.getDataNascimento());
    dto.setCpf(dependente.getCpf());
    dto.setEstado(dependente.getEstado().getCode());
    dto.setEstadoDesc(dependente.getEstado().getDescription());

    return dto;
  }

}
