package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.LeaveMobilitySubtypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveMobilitySubtype;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveMobilitySubtypeId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.util.Optional;

public interface LeaveMobilitySubtypeRepository {
    LeaveMobilitySubtype save(LeaveMobilitySubtype leaveMobilitySubtype);
    Optional<LeaveMobilitySubtype> findById(LeaveMobilitySubtypeId id);
    boolean existsByCode(String code);
    PageResult<LeaveMobilitySubtype> findAll(LeaveMobilitySubtypeFilter filter);
}
