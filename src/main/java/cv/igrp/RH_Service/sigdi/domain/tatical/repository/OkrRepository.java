package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import cv.igrp.RH_Service.sigdi.domain.tatical.models.Okr;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.OkrId;

import java.util.Optional;

public interface OkrRepository {

  Okr save(Okr okr);

  Optional<Okr> findById(OkrId id);

  Optional<Okr> findByIdFull(OkrId id);
}