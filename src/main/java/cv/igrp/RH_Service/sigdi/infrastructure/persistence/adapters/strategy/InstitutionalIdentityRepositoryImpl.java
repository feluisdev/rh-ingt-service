package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.strategy;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionalIdentityEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.InstitutionalIdentityEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.InstitutionalIdentityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InstitutionalIdentityRepositoryImpl implements InstitutionalIdentityRepository {

  private final InstitutionalIdentityEntityRepository jpaRepository;
  private final InstitutionalIdentityMapper mapper;

  @Override
  public InstitutionalIdentity save(InstitutionalIdentity identity) {
    InstitutionalIdentityEntity entity = mapper.toEntity(identity);
    InstitutionalIdentityEntity saved = jpaRepository.save(entity);
    return mapper.toDomain(saved);
  }

  @Override
  public Optional<InstitutionalIdentity> findById(InstitutionalIdentityId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomain);
  }

  @Override
  public Optional<InstitutionalIdentity> findByIdFull(InstitutionalIdentityId id) {
    return jpaRepository.findById(id.getValor().getValor())
        .map(mapper::toDomainFull);
  }
}
