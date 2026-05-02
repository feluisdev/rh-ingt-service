package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.ChangeRequestEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.ChangeRequest;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.ChangeRequestRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.ChangeRequestId;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.ChangeRequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChangeRequestRepositoryImpl implements ChangeRequestRepository {

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
}
