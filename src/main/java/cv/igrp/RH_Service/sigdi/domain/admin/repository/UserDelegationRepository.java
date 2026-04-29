package cv.igrp.RH_Service.sigdi.domain.admin.repository;

import cv.igrp.RH_Service.sigdi.domain.admin.models.Delegation;
import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.DelegationId;

import java.util.List;
import java.util.Optional;

public interface UserDelegationRepository {

  Delegation save(Delegation delegation);

  Optional<Delegation> findById(DelegationId id);

  List<Delegation> findAll();

  Delegation revoke(DelegationId id);
}
