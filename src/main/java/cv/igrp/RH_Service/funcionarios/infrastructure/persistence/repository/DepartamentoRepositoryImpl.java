package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.DepartamentoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Departamento;
import cv.igrp.RH_Service.funcionarios.domain.repository.DepartamentoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DepartamentoMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DepartamentoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.DepartamentoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DepartamentoRepositoryImpl implements DepartamentoRepository {

  private final DepartamentoEntityRepository jpaDepartamentoEntityRepository;
  private final DepartamentoMapper departamentoMapper;
  private final FuncionarioMapper funcionarioMapper;

  @Transactional
  @Override
  public Departamento save(Departamento departamento) {
    var funcionarioEntity = funcionarioMapper.toEntity(departamento.getResponsavel());
    var entity = departamentoMapper.toEntity(departamento, funcionarioEntity);

    var saved = jpaDepartamentoEntityRepository.save(entity);
    return departamentoMapper.toDomainWithResponsavel(saved, funcionarioMapper.toLightDomain(saved.getResponsavelId()));
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Departamento> getById(Integer id) {
    return jpaDepartamentoEntityRepository.findById(id)
        .map(entity -> {
          var responsavel = funcionarioMapper.toLightDomain(entity.getResponsavelId());
          return departamentoMapper.toDomainWithResponsavel(entity, responsavel);
        });
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Departamento> getByExternalId(ExternalID externalId) {
    return jpaDepartamentoEntityRepository.findByExternalId(externalId.getValor())
        .map(entity -> {
          var responsavel = funcionarioMapper.toLightDomain(entity.getResponsavelId());
          return departamentoMapper.toDomainWithResponsavel(entity, responsavel);
        });
  }

  @Transactional(readOnly = true)
  @Override
  public List<Departamento> getAll() {
    return jpaDepartamentoEntityRepository.findAllByEstado(Estado.A).stream()
        .map(entity -> {
          var responsavel = funcionarioMapper.toLightDomain(entity.getResponsavelId());
          return departamentoMapper.toDomainWithResponsavel(entity, responsavel);
        })
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Departamento> getAll(DepartamentoFilter filter) {
    var pageable = PageRequest.of(
        filter.getPageNumber() != null ? filter.getPageNumber() : 0,
        filter.getPageSize() != null ? filter.getPageSize() : 20
    );

    Specification<DepartamentoEntity> spec = (root, query, cb) -> {
      var predicates = cb.conjunction();

      if (filter.getNome() != null && !filter.getNome().isBlank()) {
        predicates = cb.and(predicates,
            cb.like(cb.lower(root.get("nome")), "%" + filter.getNome().trim().toLowerCase() + "%"));
      }

      if (filter.getResponsavelId() != null) {
        predicates = cb.and(predicates,
            cb.equal(root.get("responsavelId").get("externalId"), filter.getResponsavelId().getValor()));
      }

      if (filter.getCodigo() != null) {
        predicates = cb.and(predicates,
            cb.equal(root.get("codigo"), filter.getCodigo()));
      }

      if (filter.getEstado() != null) {
        predicates = cb.and(predicates,
            cb.equal(root.get("estado"), filter.getEstado()));
      } else {
        // opcional: filtrar apenas ativos por padrão
        predicates = cb.and(predicates,
            cb.equal(root.get("estado"), Estado.A));
      }

      return predicates;
    };

    return jpaDepartamentoEntityRepository.findAll(spec, pageable)
        .stream()
        .map(entity -> {
          var responsavel = funcionarioMapper.toLightDomain(entity.getResponsavelId());
          return departamentoMapper.toDomainWithResponsavel(entity, responsavel);
        })
        .toList();

  }

  @Transactional(readOnly = true)
  @Override
  public List<Departamento> getAllByResponsavel(ExternalID responsavelId) {
    return jpaDepartamentoEntityRepository.findAllByResponsavelId_ExternalIdAndEstado(
            responsavelId.getValor(), Estado.A
        ).stream()
        .map(entity -> {
          var responsavel = funcionarioMapper.toLightDomain(entity.getResponsavelId());
          return departamentoMapper.toDomainWithResponsavel(entity, responsavel);
        })
        .toList();
  }
}
