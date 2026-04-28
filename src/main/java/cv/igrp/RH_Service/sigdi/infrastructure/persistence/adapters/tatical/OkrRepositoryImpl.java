package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.KeyResultsEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.OkrEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.KeyResultsEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.OkrEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.Okr;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.OkrKeyResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.OkrRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.OkrId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.tatical.OkrMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OkrRepositoryImpl implements OkrRepository {

  private final OkrEntityRepository jpaOkrRepository;
  private final KeyResultsEntityRepository jpaKeyResultsRepository;
  private final OkrMapper mapper;

  @Transactional
  @Override
  public Okr save(Okr okr) {
    OkrEntity entity = mapper.toEntity(okr);
    OkrEntity savedOkr = jpaOkrRepository.save(entity);

    List<KeyResultsEntity> savedKrs = okr.getKeyResults().stream()
        .map(kr -> {
          KeyResultsEntity krEntity = mapper.krToEntity(kr, savedOkr);
          return jpaKeyResultsRepository.save(krEntity);
        })
        .collect(Collectors.toList());

    List<OkrKeyResult> domainKrs = savedKrs.stream()
        .map(mapper::krToDomain)
        .collect(Collectors.toList());

    return Okr.reconstruct(
        OkrId.from(savedOkr.getId()),
        okr.getInstitutionId(),
        okr.getStrategicGoalId(),
        savedOkr.getTitle(),
        savedOkr.getCycle(),
        savedOkr.getStatus(),
        domainKrs
    );
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Okr> findById(OkrId id) {
    return jpaOkrRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Okr> findByIdFull(OkrId id) {
    return jpaOkrRepository.findById(id.getValor().getValor())
        .map(mapper::toDomainFull);
  }
}