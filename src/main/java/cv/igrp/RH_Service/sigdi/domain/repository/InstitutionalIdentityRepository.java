package cv.igrp.RH_Service.sigdi.domain.repository;

import cv.igrp.RH_Service.sigdi.domain.models.InstitutionalIdentity;
import cv.igrp.RH_Service.sigdi.domain.valueobject.InstitutionalIdentityId;

import java.util.Optional;

public interface InstitutionalIdentityRepository {

  InstitutionalIdentity save(InstitutionalIdentity identity);

  Optional<InstitutionalIdentity> findById(InstitutionalIdentityId id);
}
