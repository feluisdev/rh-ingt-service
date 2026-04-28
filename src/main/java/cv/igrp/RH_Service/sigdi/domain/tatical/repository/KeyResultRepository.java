package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import cv.igrp.RH_Service.shared.domain.pagination.PageResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.filter.KeyResultFilter;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;

import java.util.Optional;

public interface KeyResultRepository {

  KeyResult save(KeyResult keyResult);

  Optional<KeyResult> findById(KeyResultId id);

  Optional<KeyResult> findByIdFull(KeyResultId id);

  PageResult<KeyResult> findAll(KeyResultFilter filter);
}
