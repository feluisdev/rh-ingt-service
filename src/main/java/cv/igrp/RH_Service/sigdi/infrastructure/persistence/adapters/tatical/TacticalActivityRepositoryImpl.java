package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.TacticalActivitiesEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.TacticalActivitiesEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.filter.TaticalActivityFilter;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.TacticalActivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TacticalActivityRepositoryImpl implements TacticalActivityRepository {

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
  public List<TacticalActivity> findByStatuses(List<String> statuses, int page, int size) {
    Specification<TacticalActivitiesEntity> spec = (root, query, cb) ->
        root.get("status").in(statuses);
    return jpaRepository.findAll(spec, PageRequest.of(page, size))
        .stream()
        .map(mapper::toDomain)
        .toList();
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
  public PageResult<TacticalActivity> findAll(TaticalActivityFilter filter) {
    int pageNumber = (filter != null && filter.getPageNumber() != null) ? filter.getPageNumber() : 0;
    int pageSize = (filter != null && filter.getPageSize() != null) ? filter.getPageSize() : 20;

    Pageable pageable = PageRequest.of(pageNumber, pageSize);

    Specification<TacticalActivitiesEntity> specification = (root, query, cb) -> cb.conjunction(); // a espera de futuras implementacoes

    var page = jpaRepository.findAll(specification, pageable);
    List<TacticalActivity> data = page.getContent().stream()
        .map(mapper::toDomain)
        .toList();

    return new PageResult<>(
        data,
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast()
    );
  }
}
