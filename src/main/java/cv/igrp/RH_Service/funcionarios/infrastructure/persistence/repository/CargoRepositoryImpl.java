package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.CargoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Cargo;
import cv.igrp.RH_Service.funcionarios.domain.repository.CargoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.CargoMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.CargoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.CargoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CargoRepositoryImpl implements CargoRepository {

  private final CargoEntityRepository cargoEntityRepository;
  private final CargoMapper cargoMapper;

  @Override
  public Cargo save(Cargo cargo) {
    var entity = cargoMapper.toEntity(cargo);
    var saved = cargoEntityRepository.save(entity);
    return cargoMapper.toDomain(saved);
  }

  @Override
  public Optional<Cargo> getById(Integer id) {
    return cargoEntityRepository.findById(id)
        .map(cargoMapper::toDomain);
  }

  @Override
  public Optional<Cargo> getByExternalId(ExternalID externalId) {
    return cargoEntityRepository.findByExternalId(externalId.getValor())
        .map(cargoMapper::toDomain);
  }

  @Override
  public List<Cargo> getAll() {
    return cargoEntityRepository.findAllByEstado(Estado.A)
        .stream()
        .map(cargoMapper::toDomain)
        .toList();
  }

  @Override
  public List<Cargo> getAll(CargoFilter filter) {
    var pageable = PageRequest.of(
        filter.getPageNumber() != null ? filter.getPageNumber() : 0,
        filter.getPageSize() != null ? filter.getPageSize() : 20
    );

    Specification<CargoEntity> spec = (root, query, cb) -> {
      var predicates = cb.conjunction();

      if (filter.getNome() != null && !filter.getNome().isBlank()) {
        predicates = cb.and(predicates,
            cb.like(cb.lower(root.get("nome")), "%" + filter.getNome().trim().toLowerCase() + "%"));
      }

      if (filter.getSalarioBaseMin() != null) {
        predicates = cb.and(predicates,
            cb.greaterThanOrEqualTo(root.get("salarioBase"), filter.getSalarioBaseMin()));
      }

      if (filter.getSalarioBaseMax() != null) {
        predicates = cb.and(predicates,
            cb.lessThanOrEqualTo(root.get("salarioBase"), filter.getSalarioBaseMax()));
      }

      if (filter.getNivelHierarquico() != null) {
        predicates = cb.and(predicates,
            cb.equal(root.get("nivelHierarquico"), filter.getNivelHierarquico()));
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

    var page = cargoEntityRepository.findAll(spec, pageable);
    return page.stream()
        .map(cargoMapper::toDomain)
        .toList();

  }
}
