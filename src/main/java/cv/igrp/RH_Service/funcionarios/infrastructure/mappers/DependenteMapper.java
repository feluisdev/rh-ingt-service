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


  public DependenteEntity toEntity(Dependente domain) {
    if (domain == null) {
      return null;
    }

    DependenteEntity entity = new DependenteEntity();
    entity.setId(domain.getIdDependente().getValor());
    entity.setNome(domain.getNome());
    entity.setDataNascimento(domain.getDataNascimento());
    entity.setParentesco(domain.getParentesco());
    entity.setCpf(domain.getCpf());
    entity.setEstado(domain.getEstado());

    //mapp funcionario with id
    FuncionarioEntity funcionarioEntity = new FuncionarioEntity();
    funcionarioEntity.setId(domain.getFuncionarioId().getValor());
    entity.setIdFuncionario(funcionarioEntity);

    return entity;
  }

  public Dependente toDomain(DependenteEntity entity) {
    if (entity == null) {
      return null;
    }

    return Dependente.reconstruir(
        ExternalID.from(entity.getId()),
        entity.getNome(),
        entity.getDataNascimento(),
        entity.getParentesco(),
        entity.getCpf(),
        entity.getEstado(),
        ExternalID.from(entity.getIdFuncionario().getId())
    );
  }


  public DependenteResponseDTO toResponseDTO(Dependente dependente) {
    if (dependente == null) return null;

    DependenteResponseDTO dto = new DependenteResponseDTO();
    dto.setDependenteId(dependente.getIdDependente().getStringValor());
    dto.setFuncionarioId(dependente.getFuncionarioId() != null ? dependente.getFuncionarioId().getStringValor() : null);
    dto.setNome(dependente.getNome());
    dto.setParentesco(dependente.getParentesco().getCode());
    dto.setParentescoDesc(dependente.getParentesco().getDescription());
    dto.setDataNascimento(dependente.getDataNascimento());
    dto.setCpf(dependente.getCpf());
    dto.setEstado(dependente.getEstado().getCode());
    dto.setEstadoDesc(dependente.getEstado().getDescription());

    return dto;
  }

}
