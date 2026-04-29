package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.strategy;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.StrategicGoalEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.StrategicGoalEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.StrategicGoal;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StrategicGoalRepositoryImpl implements StrategicGoalRepository {

  private final StrategicGoalEntityRepository jpaRepository;
  private final StrategicGoalMapper mapper;

  @Transactional
  @Override
  public StrategicGoal save(StrategicGoal goal) {
    StrategicGoalEntity entity = mapper.toEntity(goal);
    StrategicGoalEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<StrategicGoal> findById(StrategicGoalId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public List<StrategicGoal> findByIdentityId(InstitutionalIdentityId identityId) {
    return jpaRepository.findByIdentityId_Id(identityId.getValor().getValor()).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<StrategicGoal> findAll(InstitutionalIdentityId identityId, String perspective,
      String status, String parentGoalId, int page, int size) {
    return jpaRepository.findAll(buildSpec(identityId, perspective, status, parentGoalId),
            PageRequest.of(page, size))
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public long countAll(InstitutionalIdentityId identityId, String perspective,
      String status, String parentGoalId) {
    return jpaRepository.count(buildSpec(identityId, perspective, status, parentGoalId));
  }

  private Specification<StrategicGoalEntity> buildSpec(InstitutionalIdentityId identityId,
      String perspective, String status, String parentGoalId) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.equal(root.get("identityId").get("id"), identityId.getValor().getValor()));
      if (perspective != null && !perspective.isBlank())
        predicates.add(cb.equal(root.get("perspective"), perspective));
      if (status != null && !status.isBlank())
        predicates.add(cb.equal(root.get("status"), status));
      if (parentGoalId != null && !parentGoalId.isBlank())
        predicates.add(cb.equal(root.get("parentGoalId").get("id"), UUID.fromString(parentGoalId)));
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}
