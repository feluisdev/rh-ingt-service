package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.strategy;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionalIdentityEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.InstitutionalIdentityEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.InstitutionalIdentityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InstitutionalIdentityRepositoryImpl implements InstitutionalIdentityRepository {

  private final InstitutionalIdentityEntityRepository jpaRepository;
  private final InstitutionalIdentityMapper mapper;

  @Transactional
  @Override
  public InstitutionalIdentity save(InstitutionalIdentity identity) {
    InstitutionalIdentityEntity entity = mapper.toEntity(identity);
    InstitutionalIdentityEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<InstitutionalIdentity> findById(InstitutionalIdentityId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<InstitutionalIdentity> findByIdFull(InstitutionalIdentityId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomainFull);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<InstitutionalIdentity> findActive() {
    return jpaRepository.findFirstByIsActiveTrue()
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public List<InstitutionalIdentity> findAll(Integer cycleYear, int page, int size) {
    Specification<InstitutionalIdentityEntity> spec = buildSpec(cycleYear);
    return jpaRepository.findAll(spec, PageRequest.of(page, size))
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public long countAll(Integer cycleYear) {
    return jpaRepository.count(buildSpec(cycleYear));
  }

  private Specification<InstitutionalIdentityEntity> buildSpec(Integer cycleYear) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      if (cycleYear != null)
        predicates.add(cb.equal(root.get("cycleYear"), cycleYear));
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
