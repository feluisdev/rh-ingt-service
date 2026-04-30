package cv.igrp.RH_Service.estrutura.domain.repository;

import cv.igrp.RH_Service.estrutura.domain.filter.OrganizationalUnitFilter;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;

import java.util.List;
import java.util.Optional;

public interface OrganizationalUnitRepository {
    OrganizationalUnit save(OrganizationalUnit unit);
    Optional<OrganizationalUnit> findById(OrganizationalUnitId id);
    Optional<OrganizationalUnit> findByCode(String code);
    List<OrganizationalUnit> findAll(OrganizationalUnitFilter filter);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, OrganizationalUnitId id);
    boolean existsActiveChildrenOf(OrganizationalUnitId parentId);
}
