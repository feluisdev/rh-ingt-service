package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.funcionarios.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.FuncionarioEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FuncionarioRepositoryImpl implements FuncionarioRepository {

  private final FuncionarioEntityRepository jpaFuncionarioEntityRepository;
  private final FuncionarioMapper funcionarioMapper;

  @Override
  public Funcionario save(Funcionario funcionario) {
    var entity = funcionarioMapper.toEntity(funcionario);
    var saved = jpaFuncionarioEntityRepository.save(entity);
    return funcionarioMapper.toDomain(saved);
  }

  @Override
  public Optional<Funcionario> getbyId(Integer id) {
    return jpaFuncionarioEntityRepository.findById(id)
        .map(funcionarioMapper::toDomain);
  }

  @Override
  public Optional<Funcionario> getByExternalId(ExternalID externalId) {
    return jpaFuncionarioEntityRepository.findByExternalId(externalId.getValor()).map(
        funcionarioMapper::toDomain
    );
  }

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
        .map(funcionarioMapper::toDomain)
        .toList();
  }

  @Override
  public List<Funcionario> getAll() {
    List<FuncionarioEntity> entities = jpaFuncionarioEntityRepository.findAllByEstado(Estado.A);
    return entities.stream()
        .map(funcionarioMapper::toDomain)
        .toList();
  }
}
