package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FuncionarioEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class FuncionarioMapper {

  private final DependenteMapper dependenteMapper;
  private final QualificacaoMapper qualificacaoMapper;
  private final ContratoMapper contratoMapper;
  private final DepartamentoMapper departamentoMapper;
  private final CargoMapper cargoMapper;

  public FuncionarioMapper(DependenteMapper dependenteMapper, QualificacaoMapper qualificacaoMapper, ContratoMapper contratoMapper, DepartamentoMapper departamentoMapper, CargoMapper cargoMapper) {
    this.dependenteMapper = dependenteMapper;
    this.qualificacaoMapper = qualificacaoMapper;
    this.contratoMapper = contratoMapper;
    this.departamentoMapper = departamentoMapper;
    this.cargoMapper = cargoMapper;
  }


  public Funcionario toLightDomain(FuncionarioEntity entity) {
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


  public Funcionario toDomain(FuncionarioEntity entity) {
    if (entity == null) {
      return null;
    }
    var funcionario = Funcionario.reconstruir(
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

    // Mapeia os dependentes
    if (entity.getDependentes() != null) {
      entity.getDependentes().forEach(dep ->
          funcionario.adicionarDependente(dependenteMapper.toDomainWithFuncionario(dep, funcionario)));
    }

    if (entity.getQualificacoes() != null) {
      entity.getQualificacoes().forEach(q ->
          funcionario.adicionarQualificacao(qualificacaoMapper.toDomainWithFuncionario(q, funcionario))
      );
    }

    if (entity.getContratos() != null) {
      entity.getContratos().forEach(c ->
          funcionario.adicionarContrato(
              contratoMapper.toDomainComReferencias(c,
                  departamentoMapper.toDomainWithResponsavel(c.getIdDepartamento(), this.toLightDomain(c.getIdDepartamento().getResponsavelId())),
              funcionario, cargoMapper.toDomain(c.getIdCargo())))
      );
    }


    return funcionario;
  }

  public FuncionarioEntity toLightEntity(Funcionario funcionario) {
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

    if (funcionario.getDependentes() != null) {
      var dependentes = funcionario.getDependentes().stream()
          .map(d -> dependenteMapper.toEntity(d, entity))
          .collect(Collectors.toList());

      entity.setDependentes(dependentes);
    }

    if (funcionario.getQualificacoes() != null) {
      var qualificacoesEntities = funcionario.getQualificacoes().stream()
          .map(q -> qualificacaoMapper.toEntity(q, entity))
          .toList();
      entity.setQualificacoes(qualificacoesEntities);
    }

    if (funcionario.getContratos() != null) {
      var contratosEntities = funcionario.getContratos().stream()
          .map(c -> contratoMapper.toEntity(c, departamentoMapper.toEntity(c.getDepartamento(), this.toLightEntity(c.getDepartamento().getResponsavel())), entity, cargoMapper.toEntity(c.getCargo())))
          .toList();
      entity.setContratos(contratosEntities);
    }

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
    dto.setEstado(funcionario.getEstado() != null ? funcionario.getEstado().getCode() : null);
    dto.setEstadoDesc(funcionario.getEstado() != null ? funcionario.getEstado().getDescription() : null);

    // opcional: ajustar caso pegue essas datas da entidade JPA
    dto.setCreatedAt(null); // você pode preencher se tiver isso vindo do Entity
    dto.setUpdatedAt(null);

    return dto;
  }
}
