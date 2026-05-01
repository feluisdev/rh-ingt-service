package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.SubtipoLicencaMobilidadeFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;

import java.util.List;
import java.util.Optional;

public interface SubtipoLicencaMobilidadeRepository {
    SubtipoLicencaMobilidade save(SubtipoLicencaMobilidade subtipo);
    Optional<SubtipoLicencaMobilidade> findById(SubtipoLicencaMobilidadeId id);
    List<SubtipoLicencaMobilidade> findAll(SubtipoLicencaMobilidadeFilter filter);
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, SubtipoLicencaMobilidadeId id);
}
