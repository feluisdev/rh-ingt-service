package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.DependenteFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Dependente;
import cv.igrp.RH_Service.funcionarios.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.DependenteMapper;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.DependenteEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.DependenteEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DependenteRepositoryImpl implements DependenteRepository {

  private final DependenteMapper dependenteMapper;
  private final DependenteEntityRepository dependenteEntityRepository;
  private final FuncionarioMapper funcionarioMapper;


  @Transactional
  @Override
  public Dependente save(Dependente dependente) {
    var funcionarioEntity = funcionarioMapper.toEntity(dependente.getFuncionario());
    var entity = dependenteMapper.toEntity(dependente, funcionarioEntity);
    var saved = dependenteEntityRepository.save(entity);

    var funcionarioDomain = funcionarioMapper.toDomain(funcionarioEntity);
    return dependenteMapper.toDomainWithFuncionario(saved, funcionarioDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Dependente> getById(Integer id) {
    return Optional.empty();
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Dependente> getByExternalId(ExternalID externalId) {

    return dependenteEntityRepository.findByExternalId(externalId.getValor())
        .map(entity -> {

          var funcionario = funcionarioMapper.toDomain(entity.getIdFuncionario());
          return dependenteMapper.toDomainWithFuncionario(entity, funcionario);

        });
  }


  @Transactional(readOnly = true)
  @Override
  public List<Dependente> getAll() {
    var entities = dependenteEntityRepository.findAllByEstado(Estado.A);
    return entities.stream()
        .map(entity -> {
          var funcionario = funcionarioMapper.toDomain(entity.getIdFuncionario());
          return dependenteMapper.toDomainWithFuncionario(entity, funcionario);
        })
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Dependente> getAll(DependenteFilter filter) {
    var pageable = PageRequest.of(
        filter.getPageNumber() != null ? filter.getPageNumber() : 0,
        filter.getPageSize() != null ? filter.getPageSize() : 20
    );

    Specification<DependenteEntity> spec = (root, query, cb) -> {
      var predicates = cb.conjunction();

      if (filter.getFuncionarioExternalId() != null) {
        predicates = cb.and(predicates,
            cb.equal(root.get("idFuncionario").get("externalId"), filter.getFuncionarioExternalId().getValor()));
      }

      if (filter.getNome() != null && !filter.getNome().isBlank()) {
        predicates = cb.and(predicates,
            cb.like(cb.lower(root.get("nome")), "%" + filter.getNome().trim().toLowerCase() + "%"));
      }

      predicates = cb.and(predicates, cb.equal(root.get("estado"), Estado.A));

      return predicates;
    };

    var page = dependenteEntityRepository.findAll(spec, pageable);

    return page.stream()
        .map(entity -> {
          var funcionario = funcionarioMapper.toDomain(entity.getIdFuncionario());
          return dependenteMapper.toDomainWithFuncionario(entity, funcionario);
        })
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Dependente> getAllByFuncionarioExternalId(ExternalID funcionarioExternalId) {
    var entities = dependenteEntityRepository.findAllByIdFuncionario_ExternalId_AndEstado(funcionarioExternalId.getValor(), Estado.A);
    return entities.stream()
        .map(entity -> {
          var funcionario = funcionarioMapper.toDomain(entity.getIdFuncionario());
          return dependenteMapper.toDomainWithFuncionario(entity, funcionario);
        })
        .toList();
  }
}
