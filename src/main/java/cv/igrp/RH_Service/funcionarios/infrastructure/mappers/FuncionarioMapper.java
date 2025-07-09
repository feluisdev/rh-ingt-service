package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FuncionarioEntity;
import org.springframework.stereotype.Component;

@Component
public class FuncionarioMapper {

  public Funcionario toDomain(FuncionarioEntity entity) {
    if (entity == null) {
      return null;
    }
    return Funcionario.reconstruir(
        entity.getId(),
        ExternalID.from(entity.getExternalId()),
        entity.getNome(),
        entity.getNif(),
        entity.getNumSegurado(),
        entity.getNib(),
        entity.getEmail(),
        entity.getEstado(),
        entity.getSexo(),
        entity.getEstadoCivil(),
        entity.getEndereco()

    );
  }

  public FuncionarioEntity toEntity(Funcionario funcionario) {
    if (funcionario == null) {
      return null;
    }
    FuncionarioEntity entity = new FuncionarioEntity();

    if (funcionario.getId() != null) {
      entity.setId(funcionario.getId());
    }
    entity.setExternalId(funcionario.getExternalId().getValor());
    entity.setNome(funcionario.getNome());
    entity.setNif(funcionario.getNif() != null ? funcionario.getNif().getValor() : null);
    entity.setNumSegurado(funcionario.getNumSegurado() != null ? funcionario.getNumSegurado().getValor() : null);
    entity.setNib(funcionario.getNib() != null ? funcionario.getNib().getValor() : null);
    entity.setEmail(funcionario.getEmail() != null ? funcionario.getEmail().getValor() : null);
    entity.setEstado(funcionario.getEstado());
    entity.setSexo(funcionario.getSexo());
    entity.setEstadoCivil(funcionario.getEstadoCivil());
    entity.setEndereco(funcionario.getEndereco());
    return entity;
  }

  public FuncionarioResponseDTO toResponseDTO(Funcionario funcionario) {
    if (funcionario == null) {
      return null;
    }

    FuncionarioResponseDTO dto = new FuncionarioResponseDTO();
    dto.setExternalID(funcionario.getExternalId().getStringValor());
    dto.setNome(funcionario.getNome());
    dto.setNif(funcionario.getNif() != null ? funcionario.getNif().getValor() : null);
    dto.setNumSegurado(funcionario.getNumSegurado() != null ? funcionario.getNumSegurado().getValor() : null);
    dto.setNib(funcionario.getNib() != null ? funcionario.getNib().getValor() : null);
    dto.setEmail(funcionario.getEmail() != null ? funcionario.getEmail().getValor() : null);
    dto.setSexo(funcionario.getSexo() != null ? funcionario.getSexo().name() : null);
    dto.setEstadoCivil(funcionario.getEstadoCivil() != null ? funcionario.getEstadoCivil().name() : null);
    dto.setEndereco(funcionario.getEndereco());

    // opcional: ajustar caso pegue essas datas da entidade JPA
    dto.setCreatedAt(null); // você pode preencher se tiver isso vindo do Entity
    dto.setUpdatedAt(null);

    return dto;
  }
}
