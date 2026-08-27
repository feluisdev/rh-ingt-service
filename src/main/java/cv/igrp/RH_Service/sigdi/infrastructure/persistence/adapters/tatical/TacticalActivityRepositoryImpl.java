package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.filter.TaticalActivityFilter;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PendingActivityRow;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.TacticalActivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TacticalActivityRepositoryImpl implements TacticalActivityRepository {

  // Explicit order (D-S, 110-01-PLAN.md): without it, PageRequest.of alone leaves page k and
  // k+1 free to repeat or skip a row, which the Plan 02 combined pagination would amplify
  // across two sources.
  private static final Sort OLDEST_FIRST = Sort.by(Sort.Direction.ASC, "createdDate");

  private final TacticalActivitiesEntityRepository jpaRepository;
  private final TacticalActivityMapper mapper;

  @Transactional
  @Override
  public TacticalActivity save(TacticalActivity activity) {
    TacticalActivitiesEntity entity = mapper.toEntity(activity);
    TacticalActivitiesEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<TacticalActivity> findById(TacticalActivityId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<TacticalActivity> findByIdFull(TacticalActivityId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomainFull);
  }

  @Transactional(readOnly = true)
  @Override
  public long countByStatuses(List<String> statuses) {
    Specification<TacticalActivitiesEntity> spec = (root, query, cb) ->
        root.get("status").in(statuses);
    return jpaRepository.count(spec);
  }

  @Transactional(readOnly = true)
  @Override
  public List<PendingActivityRow> findPendingRows(List<String> statuses, int page, int size) {
    Specification<TacticalActivitiesEntity> spec = (root, query, cb) ->
        root.get("status").in(statuses);
    return jpaRepository.findAll(spec, PageRequest.of(page, size, OLDEST_FIRST))
        .stream()
        .map(this::toRow)
        .toList();
  }

  private PendingActivityRow toRow(TacticalActivitiesEntity entity) {
    return new PendingActivityRow(
        entity.getId(),
        entity.getTitle(),
        entity.getStatus(),
        entity.getBudgetEstimated(),
        entity.getEconomicClassifier(),
        entity.getCreatedBy(),
        entity.getCreatedDate());
  }

  @Transactional(readOnly = true)
  @Override
  public PageResult<TacticalActivity> findAll(TaticalActivityFilter filter) {
    int pageNumber = (filter != null && filter.getPageNumber() != null) ? filter.getPageNumber() : 0;
    int pageSize = (filter != null && filter.getPageSize() != null) ? filter.getPageSize() : 20;

    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    Specification<TacticalActivitiesEntity> specification = (root, query, cb) -> {
      var predicate = cb.conjunction();
      if (filter != null && filter.getPaaLevel() != null && !filter.getPaaLevel().isBlank()) {
        predicate = cb.and(predicate, cb.equal(root.get("paaLevel"), filter.getPaaLevel()));
      }
      return predicate;
    };

    var page = jpaRepository.findAll(specification, pageable);
    List<TacticalActivity> data = page.getContent().stream()
        .map(mapper::toDomain)
        .toList();

    return new PageResult<>(data, page.getNumber(), page.getSize(), page.getTotalElements(),
        page.getTotalPages(), page.isFirst(), page.isLast());
  }

  // Fase 119 (PRZ-05): guarda de nulos primeiro -- year nulo ou paaLevel nulo devolvem lista
  // vazia sem tocar no JPA, para que um chamador que não tenha os dois valores prontos não
  // provoque uma query sem sentido.
  @Transactional(readOnly = true)
  @Override
  public List<UUID> findOrganicUnitIdsWithActivitiesInYear(Integer year, PaaLevel paaLevel) {
    if (year == null || paaLevel == null) {
      return List.of();
    }
    // paaLevel.getCode() e nunca toString() -- a coluna t_tactical_activities.paa_level guarda
    // o código do enum, não a sua representação Java.
    return jpaRepository.findDistinctOrganicUnitIdsByFiscalYearAndPaaLevel(year, paaLevel.getCode());
  }
}
