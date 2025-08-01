package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.DepartamentoResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Departamento;
import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DepartamentoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FuncionarioEntity;
import org.springframework.stereotype.Component;

@Component
public class DepartamentoMapper {

  public Departamento toDomainWithResponsavel(DepartamentoEntity entity, Funcionario responsavel) {
    if (entity == null) return null;

    return Departamento.reconstruir(
        ExternalID.from(entity.getId()),
        entity.getNome(),
        entity.getCodigo(),
        entity.getDescricao(),
        entity.getLocalizacao(),
        entity.getOrcamento(),
        entity.getEstado(),
        null
    );
  }

  public DepartamentoEntity toEntity(Departamento domain, FuncionarioEntity responsavelEntity) {
    if (domain == null) return null;

    DepartamentoEntity entity = new DepartamentoEntity();

    entity.setId(domain.getIdDepartamento().getValor());
    entity.setNome(domain.getNome());
    entity.setCodigo(domain.getCodigo());
    entity.setDescricao(domain.getDescricao());
    entity.setLocalizacao(domain.getLocalizacao());
    entity.setOrcamento(domain.getOrcamento());
    entity.setEstado(domain.getEstado());
    entity.setResponsavelId(responsavelEntity);

    return entity;
  }

  public DepartamentoEntity toEntity(Departamento domain) {
    if (domain == null) return null;

    DepartamentoEntity entity = new DepartamentoEntity();

    entity.setId(domain.getIdDepartamento().getValor());
    entity.setNome(domain.getNome());
    entity.setCodigo(domain.getCodigo());
    entity.setDescricao(domain.getDescricao());
    entity.setLocalizacao(domain.getLocalizacao());
    entity.setOrcamento(domain.getOrcamento());
    entity.setEstado(domain.getEstado());

    FuncionarioEntity responsavelEntity = new FuncionarioEntity();
    responsavelEntity.setId(domain.getResponsavelID().getValor());
    entity.setResponsavelId(responsavelEntity);

    return entity;
  }

  public Departamento toDomain(DepartamentoEntity entity) {
    if (entity == null) return null;

    return Departamento.reconstruir(
        ExternalID.from(entity.getId()),
        entity.getNome(),
        entity.getCodigo(),
        entity.getDescricao(),
        entity.getLocalizacao(),
        entity.getOrcamento(),
        entity.getEstado(),
        ExternalID.from(entity.getResponsavelId().getId())
    );
  }

  public DepartamentoResponseDTO toDTO(Departamento departamento) {
    if (departamento == null) {
      return null;
    }

    DepartamentoResponseDTO dto = new DepartamentoResponseDTO();

    dto.setDepartamentoId(departamento.getIdDepartamento() != null ? departamento.getIdDepartamento().getStringValor() : null);

    dto.setResponsavelId(
        departamento.getResponsavelID() != null
            ? departamento.getResponsavelID().getStringValor()
            : null
    );

    dto.setNome(departamento.getNome());
    dto.setDescricao(departamento.getDescricao());
    dto.setCodigo(departamento.getCodigo());
    dto.setOrcamento(departamento.getOrcamento());

    dto.setEstado(departamento.getEstado() != null ? departamento.getEstado().name() : null);
    dto.setEstadoDesc(departamento.getEstado() != null ? departamento.getEstado().getDescription() : null);

    return dto;
  }

}
