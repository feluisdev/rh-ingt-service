package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.ProfessionalSituationFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ProfessionalSituation;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface ProfessionalSituationRepository {
    ProfessionalSituation save(ProfessionalSituation professionalSituation);
    Optional<ProfessionalSituation> findById(ExternalID id);
    boolean existsByCode(String code);
    List<ProfessionalSituation> findAll(ProfessionalSituationFilter filter);
}
