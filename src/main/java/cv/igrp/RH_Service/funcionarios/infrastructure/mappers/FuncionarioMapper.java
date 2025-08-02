package cv.igrp.RH_Service.funcionarios.infrastructure.mappers;

import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioDetailsDTO;
import cv.igrp.RH_Service.funcionarios.application.dto.FuncionarioResponseDTO;
import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DocumentoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FuncionarioEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class FuncionarioMapper {

  private final DependenteMapper dependenteMapper;
  private final QualificacaoMapper qualificacaoMapper;
  private final ContratoMapper contratoMapper;
  private final DepartamentoMapper departamentoMapper;
  private final CargoMapper cargoMapper;

  private final DocumentoMapper documentoMapper;

  public FuncionarioMapper(DependenteMapper dependenteMapper, QualificacaoMapper qualificacaoMapper, ContratoMapper contratoMapper, DepartamentoMapper departamentoMapper, CargoMapper cargoMapper, DocumentoMapper documentoMapper) {
    this.dependenteMapper = dependenteMapper;
    this.qualificacaoMapper = qualificacaoMapper;
    this.contratoMapper = contratoMapper;
    this.departamentoMapper = departamentoMapper;
    this.cargoMapper = cargoMapper;
    this.documentoMapper = documentoMapper;
  }


  public Funcionario toLightDomain(FuncionarioEntity entity) {
    if (entity == null) {
      return null;
    }
    return Funcionario.reconstruir(
        ExternalID.from(entity.getId()),
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


  public Funcionario toDomain(FuncionarioEntity entity, List<DocumentoEntity> documentos) {
    if (entity == null) {
      return null;
    }
    var funcionario = Funcionario.reconstruir(
        ExternalID.from(entity.getId()),
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
          funcionario.adicionarDependente(dependenteMapper.toDomain(dep)));
    }

    /*if (entity.getQualificacoes() != null) {
      entity.getQualificacoes().forEach(q ->
          funcionario.adicionarQualificacao(qualificacaoMapper.toDomain(q))
      );
    }*/

    /*if (entity.getContratos() != null) {
      entity.getContratos().forEach(c ->
          funcionario.adicionarContrato(
              contratoMapper.toDomain(c))
      );
    }*/

    // Qualificações COM anexo
    if (entity.getQualificacoes() != null) {
      entity.getQualificacoes().forEach(qualificacao -> {
        DocumentoEntity anexoQualificacao = findDocumentoByObjectIdAndTipo(documentos, qualificacao.getId(), ObjetoTipo.QUALIFICACAO);
        funcionario.adicionarQualificacao(qualificacaoMapper.toDomain(qualificacao, anexoQualificacao));
      });
    }

    if (entity.getContratos() != null) {
      entity.getContratos().forEach(contrato -> {
        // Encontra o documento do contrato na lista geral
        DocumentoEntity anexoContrato = findDocumentoByObjectIdAndTipo(documentos, contrato.getId(), ObjetoTipo.CONTRATO);
        // Mapeia passando o anexo (ajuste o contratoMapper para suportar isso)
        funcionario.adicionarContrato(contratoMapper.toDomain(contrato, anexoContrato));
      });
    }

    // Apenas os documentos do FUNCIONARIO
    if (documentos != null && !documentos.isEmpty()) {
      documentos.stream()
          .filter(doc -> doc.getObjectoTipo() == ObjetoTipo.FUNCIONARIO)
          .map(documentoMapper::toDomain)
          .forEach(funcionario::adicionarDocumento);
    }

    return funcionario;
  }

  // Método auxiliar para achar documento na lista (retorna null se não achar)
  private DocumentoEntity findDocumentoByObjectIdAndTipo(List<DocumentoEntity> documentos, UUID objectId, ObjetoTipo tipo) {
    if (documentos == null || documentos.isEmpty()) return null;
    return documentos.stream()
        .filter(doc -> doc.getObjectoTipo() == tipo && doc.getObjectId().equals(objectId))
        .findFirst()
        .orElse(null);
  }

  public FuncionarioEntity toLightEntity(Funcionario funcionario) {
    if (funcionario == null) {
      return null;
    }
    FuncionarioEntity entity = new FuncionarioEntity();


    entity.setId(funcionario.getIdFuncionario().getValor());
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

    entity.setId(funcionario.getIdFuncionario().getValor());
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
          .map(dependenteMapper::toEntity)
          .collect(Collectors.toList());

      entity.setDependentes(dependentes);
    }

    if (funcionario.getQualificacoes() != null) {
      var qualificacoesEntities = funcionario.getQualificacoes().stream()
          .map(qualificacaoMapper::toEntity)
          .toList();
      entity.setQualificacoes(qualificacoesEntities);
    }

   if (funcionario.getContratos() != null) {
      var contratosEntities = funcionario.getContratos().stream()
          .map(contratoMapper::toEntity)
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
    dto.setFuncionarioId(funcionario.getIdFuncionario().getStringValor());
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

    if (funcionario.getDocumentos() != null && !funcionario.getDocumentos().isEmpty()) {
      dto.setAnexos(
          funcionario.getDocumentos().stream()
              .map(documentoMapper::toDTO)
              .toList()
      );
    }

    return dto;
  }

  public FuncionarioDetailsDTO toResponseDetails(Funcionario funcionario) {
    if (funcionario == null) {
      return null;
    }

    var dto = new FuncionarioDetailsDTO();
    dto.setFuncionarioId(funcionario.getIdFuncionario().getStringValor());
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

    if (funcionario.getQualificacoes() != null) {
      dto.setQualificacoes(
          funcionario.getQualificacoes().stream()
              .map(qualificacaoMapper::toDTO)
              .toList()
      );

    }

    if (funcionario.getDependentes() != null) {
      dto.setDependentes(
          funcionario.getDependentes().stream()
              .map(dependenteMapper::toResponseDTO)
              .toList()
      );
    }

    // Contrato atual (supondo que o último da lista seja o atual)
    if (funcionario.getContratos() != null && !funcionario.getContratos().isEmpty()) {
      var contratoAtual = funcionario.getContratos()
          .getLast(); // ou criar regra no domínio para pegar o "ativo"
      dto.setContratoAtual(contratoMapper.toDTO(contratoAtual));
    }

    if (funcionario.getDocumentos() != null) {
      dto.setAnexos(
          funcionario.getDocumentos().stream()
              .map(documentoMapper::toDTO)
              .toList()
      );
    }


    return dto;
  }
}
