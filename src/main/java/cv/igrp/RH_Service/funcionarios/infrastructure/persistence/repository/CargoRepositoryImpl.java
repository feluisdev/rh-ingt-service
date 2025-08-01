package cv.igrp.RH_Service.funcionarios.infrastructure.persistence.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.CargoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Cargo;
import cv.igrp.RH_Service.funcionarios.domain.repository.CargoRepository;
import cv.igrp.RH_Service.funcionarios.infrastructure.mappers.CargoMapper;
import cv.igrp.RH_Service.shared.application.constants.Estado;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.CargoEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.CargoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CargoRepositoryImpl implements CargoRepository {

  private final CargoEntityRepository cargoEntityRepository;
  private final CargoMapper cargoMapper;

  @Transactional
  @Override
  public Cargo save(Cargo cargo) {
    var entity = cargoMapper.toEntity(cargo);
    var saved = cargoEntityRepository.save(entity);
    return cargoMapper.toDomain(saved);
  }


  @Transactional(readOnly = true)
  @Override
  public Optional<Cargo> getByExternalId(ExternalID externalId) {
    return cargoEntityRepository.findByExternalId(externalId.getValor())
        .map(cargoMapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public List<Cargo> getAll() {
    return cargoEntityRepository.findAllByEstado(Estado.A)
        .stream()
        .map(cargoMapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
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
          Estado estadoEnum = Estado.valueOf(filter.getEstado().toUpperCase());
          predicates = cb.and(predicates, cb.equal(root.get("estado"), estadoEnum));
      } else {
        predicates = cb.and(predicates, cb.equal(root.get("estado"), Estado.A));
      }

      return predicates;
    };

    var page = cargoEntityRepository.findAll(spec, pageable);
    return page.stream()
        .map(cargoMapper::toDomain)
        .toList();

  }
}
