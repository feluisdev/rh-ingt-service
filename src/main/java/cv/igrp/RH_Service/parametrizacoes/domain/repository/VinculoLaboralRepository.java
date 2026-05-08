package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.VinculoLaboralFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.VinculoLaboral;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.VinculoLaboralId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.util.Optional;

public interface VinculoLaboralRepository {
    VinculoLaboral save(VinculoLaboral vinculoLaboral);
    Optional<VinculoLaboral> findById(VinculoLaboralId id);
    boolean existsByCode(String code);
    PageResult<VinculoLaboral> findAll(VinculoLaboralFilter filter);
}
