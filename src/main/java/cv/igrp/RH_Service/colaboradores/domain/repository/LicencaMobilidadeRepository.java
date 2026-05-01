package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.LicencaMobilidadeFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;

import java.util.List;
import java.util.Optional;

public interface LicencaMobilidadeRepository {

    LicencaMobilidade save(LicencaMobilidade licenca);

    Optional<LicencaMobilidade> findById(LicencaMobilidadeId id);

    List<LicencaMobilidade> findAllByFuncionarioId(FuncionarioId funcionarioId, LicencaMobilidadeFilter filter);
}
