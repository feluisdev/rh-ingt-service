package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.admin;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.UserDelegationEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.UserDelegationEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.admin.models.Delegation;
import cv.igrp.RH_Service.sigdi.domain.admin.repository.UserDelegationRepository;
import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.DelegationId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.admin.DelegationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

  @Transactional(readOnly = true)
  @Override
  public List<Delegation> findAll() {
    return jpaRepository.findAll().stream()
        .map(mapper::toDomain)
        .collect(Collectors.toList());
  }

  @Transactional
  @Override
  public Delegation revoke(DelegationId id) {
    UserDelegationEntity entity = jpaRepository.findById(id.getValor().getValor())
        .orElseThrow(() -> IgrpResponseStatusException.of(HttpStatus.NOT_FOUND,
            "Delegation not found: " + id.getStringValor()));
    Delegation domain = mapper.toDomain(entity);
    Delegation revoked = domain.revoke();
    UserDelegationEntity revokedEntity = mapper.toEntity(revoked);
    UserDelegationEntity saved = jpaRepository.save(revokedEntity);
    return mapper.toDomain(saved);
  }
}
