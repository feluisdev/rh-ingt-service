package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.ContractTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ContractType;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ContractTypeId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.util.Optional;

public interface ContractTypeRepository {
    ContractType save(ContractType contractType);
    Optional<ContractType> findById(ContractTypeId id);
    boolean existsByCode(String code);
    PageResult<ContractType> findAll(ContractTypeFilter filter);
}
