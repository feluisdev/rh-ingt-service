package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.ProfessionalSituationFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ProfessionalSituation;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ProfessionalSituationId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.util.Optional;

public interface ProfessionalSituationRepository {
    ProfessionalSituation save(ProfessionalSituation professionalSituation);
    Optional<ProfessionalSituation> findById(ProfessionalSituationId id);
    boolean existsByCode(String code);
    PageResult<ProfessionalSituation> findAll(ProfessionalSituationFilter filter);
}
