package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.InstitutionalIdentityEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.InstitutionalIdentityEntityRepository;
import cv.igrp.RH_Service.sigdi.domain.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.repository.InstitutionalIdentityRepository;
import cv.igrp.RH_Service.sigdi.domain.valueobject.InstitutionalIdentityId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.InstitutionalIdentityMapper;
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
}
