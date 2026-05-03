package cv.igrp.RH_Service.estrutura.domain.repository;

import cv.igrp.RH_Service.estrutura.domain.filter.FunctionFilter;
import cv.igrp.RH_Service.estrutura.domain.models.OrgFunction;
import cv.igrp.RH_Service.estrutura.domain.valueobject.FunctionId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.util.Optional;

public interface FunctionRepository {
    OrgFunction save(OrgFunction function);
    Optional<OrgFunction> findById(FunctionId id);
    Optional<OrgFunction> findByCode(String code);
    PageResult<OrgFunction> findAll(FunctionFilter filter);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, FunctionId id);
}
