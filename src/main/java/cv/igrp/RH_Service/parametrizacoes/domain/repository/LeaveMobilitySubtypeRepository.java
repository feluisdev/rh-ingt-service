package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.LeaveMobilitySubtypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveMobilitySubtype;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface LeaveMobilitySubtypeRepository {
    LeaveMobilitySubtype save(LeaveMobilitySubtype leaveMobilitySubtype);
    Optional<LeaveMobilitySubtype> findById(ExternalID id);
    boolean existsByCode(String code);
    List<LeaveMobilitySubtype> findAll(LeaveMobilitySubtypeFilter filter);
}
