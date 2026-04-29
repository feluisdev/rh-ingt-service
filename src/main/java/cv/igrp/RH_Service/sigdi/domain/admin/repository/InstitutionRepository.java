package cv.igrp.RH_Service.sigdi.domain.admin.repository;

import cv.igrp.RH_Service.sigdi.domain.admin.models.Institution;
import cv.igrp.RH_Service.sigdi.domain.admin.valueobject.InstitutionId;

import java.util.List;
import java.util.Optional;

public interface InstitutionRepository {

  Institution save(Institution institution);

  Optional<Institution> findById(InstitutionId id);

  Optional<Institution> findByCode(String code);

  List<Institution> findAll();

  Institution update(Institution institution);
}
