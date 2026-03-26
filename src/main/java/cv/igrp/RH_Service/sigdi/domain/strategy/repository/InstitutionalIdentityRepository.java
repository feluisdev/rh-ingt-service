package cv.igrp.RH_Service.sigdi.domain.strategy.repository;

import cv.igrp.RH_Service.sigdi.domain.strategy.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.InstitutionalIdentityId;

import java.util.Optional;

public interface InstitutionalIdentityRepository {

  InstitutionalIdentity save(InstitutionalIdentity identity);

  Optional<InstitutionalIdentity> findById(InstitutionalIdentityId id);

  Optional<InstitutionalIdentity> findByIdFull(InstitutionalIdentityId id);

  Optional<InstitutionalIdentity> findActive();

}
