package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.application.constants.ObjetoTipo;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DocumentoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.DocumentoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.FuncionarioEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FuncionarioRepositoryImpl implements FuncionarioRepository {

  private final FuncionarioEntityRepository jpaFuncionarioEntityRepository;
  private final FuncionarioMapper funcionarioMapper;
  private final DocumentoEntityRepository documentoEntityRepository;

  private final DocumentoMapper documentoMapper;
  private final FuncionarioEntityRepository funcionarioEntityRepository;

  @Transactional
  @Override
  public Funcionario save(Funcionario funcionario) {
    var entity = funcionarioMapper.toEntity(funcionario);
    var saved = jpaFuncionarioEntityRepository.save(entity);

    List<DocumentoEntity> documentoEntities = List.of();

    if(funcionario.getDocumentos() != null && !funcionario.getDocumentos().isEmpty()) {
       documentoEntities = funcionario.getDocumentos().stream()
          .map(documentoMapper::toEntity)
          .collect(Collectors.toList());
      documentoEntityRepository.saveAll(documentoEntities);
    }

    return funcionarioMapper.toDomain(saved, documentoEntities);
  }


  @Transactional(readOnly = true)
  @Override
  public Optional<Funcionario> getById(ExternalID funcionarioId) {
    return jpaFuncionarioEntityRepository.findById(funcionarioId.getValor())
          .map(entity -> {
          List<DocumentoEntity> documentos = documentoEntityRepository.findByObjectIdAndObjectoTipo(funcionarioId.getValor(), ObjetoTipo.FUNCIONARIO);
          return funcionarioMapper.toDomain(entity, documentos);
        });
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Funcionario> getByIdWithDetails(ExternalID idFuncionario) {
    return jpaFuncionarioEntityRepository.findById(idFuncionario.getValor())
        .map(entity -> {
          var funcionarioId = idFuncionario.getValor();

          // Documentos do FUNCIONARIO
          List<DocumentoEntity> documentosFuncionario =
              documentoEntityRepository.findByObjectIdAndObjectoTipo(funcionarioId, ObjetoTipo.FUNCIONARIO);

          // Documento de cada CONTRATO do funcionario (1 anexo por contrato)
          List<DocumentoEntity> documentosContratos = entity.getContratos().stream()
              .map(contrato ->
                  documentoEntityRepository.findFirstByObjectIdAndObjectoTipo(contrato.getId(), ObjetoTipo.CONTRATO)
              )
              .flatMap(Optional::stream) // só pega os presentes
              .toList();

          // Documento de cada QUALIFICACAO do funcionario (1 anexo por qualificacao)
          List<DocumentoEntity> documentosQualificacoes = entity.getQualificacoes().stream()
              .map(qualificacao ->
                  documentoEntityRepository.findFirstByObjectIdAndObjectoTipo(qualificacao.getId(), ObjetoTipo.QUALIFICACAO)
              )
              .flatMap(Optional::stream)
              .toList();

          List<DocumentoEntity> todosDocumentos = new ArrayList<>();
          todosDocumentos.addAll(documentosFuncionario);
          todosDocumentos.addAll(documentosContratos);
          todosDocumentos.addAll(documentosQualificacoes);

          return funcionarioMapper.toDomain(entity, todosDocumentos);
        });
  }


  @Transactional(readOnly = true)
  @Override
  public List<Funcionario> getAll(FuncionarioFilter filter) {
    var pageable = PageRequest.of(
        filter.getPageNumber() != null ? filter.getPageNumber() : 0,
        filter.getPageSize() != null ? filter.getPageSize() : 20
    );

    Specification<FuncionarioEntity> spec = (root, query, cb) -> {
      var predicates = cb.conjunction();

      if (filter.getNome() != null && !filter.getNome().isBlank()) {
        predicates = cb.and(predicates,
            cb.like(cb.lower(root.get("nome")), "%" + filter.getNome().trim().toLowerCase() + "%"));
      }

      if (filter.getNif() != null && !filter.getNif().isBlank()) {
        predicates = cb.and(predicates,
            cb.equal(root.get("nif"), filter.getNif().trim()));
      }

      if (filter.getNumSegurado() != null && !filter.getNumSegurado().isBlank()) {
        predicates = cb.and(predicates,
            cb.equal(root.get("numSegurado"), filter.getNumSegurado().trim()));
      }

      if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
        predicates = cb.and(predicates,
            cb.equal(root.get("email"), filter.getEmail().trim()));
      }

      // Opcional: trazer apenas ativos
      predicates = cb.and(predicates, cb.equal(root.get("estado"), Estado.A));

      return predicates;
    };

    var page = jpaFuncionarioEntityRepository.findAll(spec, pageable);

    return page.stream()
        .map(funcionarioMapper::toLightDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Funcionario> getAll() {
    List<FuncionarioEntity> entities = jpaFuncionarioEntityRepository.findAllByEstado(Estado.A);
    return entities.stream()
        .map(funcionarioMapper::toLightDomain)
        .toList();
  }

  @Override
  public boolean existsById(ExternalID idFuncionario) {
    return funcionarioEntityRepository.existsById(idFuncionario.getValor());
  }
}
