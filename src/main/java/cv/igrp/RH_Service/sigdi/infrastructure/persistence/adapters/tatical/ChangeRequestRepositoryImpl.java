package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import cv.igrp.RH_Service.sigdi.application.constants.ChangeRequestStatus;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.ChangeRequestEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.ChangeRequestEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.ChangeRequest;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.ChangeRequestRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PendingChangeRequestRow;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.ChangeRequestId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.ChangeRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChangeRequestRepositoryImpl implements ChangeRequestRepository {

  // Explicit order (D-S, 110-01-PLAN.md): without it, PageRequest.of alone leaves page k and
  // k+1 free to repeat or skip a row, which the Plan 02 combined pagination would amplify
  // across two sources.
  private static final Sort OLDEST_FIRST = Sort.by(Sort.Direction.ASC, "createdDate");

  private final ChangeRequestEntityRepository jpaRepository;
  private final ChangeRequestMapper mapper;

  @Transactional
  @Override
  public ChangeRequest save(ChangeRequest changeRequest) {
    var entity = mapper.toEntity(changeRequest);
    var saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<ChangeRequest> findById(ChangeRequestId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public List<ChangeRequest> findByActivityId(TacticalActivityId activityId) {
    return jpaRepository.findAll((root, query, cb) ->
            cb.equal(root.get("activityId").get("id"), activityId.getValor().getValor()))
        .stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public List<PendingChangeRequestRow> findPendingRows(int page, int size) {
    return jpaRepository.findPendingWithActivity(
            ChangeRequestStatus.PENDING.getCode(), PageRequest.of(page, size, OLDEST_FIRST))
        .stream()
        .map(this::toRow)
        .toList();
  }

  @Transactional(readOnly = true)
  @Override
  public long countPending() {
    return jpaRepository.countByStatus(ChangeRequestStatus.PENDING.getCode());
  }

  private PendingChangeRequestRow toRow(ChangeRequestEntity entity) {
    return new PendingChangeRequestRow(
        entity.getId(),
        entity.getActivityId().getId(),
        entity.getActivityId().getTitle(),
        entity.getFieldName(),
        entity.getCurrentValue(),
        entity.getProposedValue(),
        entity.getCreatedBy(),
        entity.getCreatedDate());
  }
}
