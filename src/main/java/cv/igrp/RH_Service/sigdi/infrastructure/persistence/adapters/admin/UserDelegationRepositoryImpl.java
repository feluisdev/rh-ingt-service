package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.admin;

import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.UserDelegationEntity;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.repository.UserDelegationEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.admin.models.Delegation;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.UserDelegationRepository;
import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.DelegationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.admin.DelegationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDelegationRepositoryImpl implements UserDelegationRepository {

  private final UserDelegationEntityRepository jpaRepository;
  private final DelegationMapper mapper;

  @Transactional
  @Override
  public Delegation save(Delegation delegation) {
    UserDelegationEntity entity = mapper.toEntity(delegation);
    UserDelegationEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<Delegation> findById(DelegationId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }
}
