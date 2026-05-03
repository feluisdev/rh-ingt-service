package cv.igrp.RH_Service.estrutura.domain.repository;

import cv.igrp.RH_Service.estrutura.domain.filter.OrganizationalUnitFilter;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrganizationalUnitRepository {
    OrganizationalUnit save(OrganizationalUnit unit);
    Optional<OrganizationalUnit> findById(OrganizationalUnitId id);
    Optional<OrganizationalUnit> findByCode(String code);
    PageResult<OrganizationalUnit> findAll(OrganizationalUnitFilter filter);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, OrganizationalUnitId id);
    boolean existsActiveChildrenOf(OrganizationalUnitId parentId);
    List<OrganizationalUnit> findAllByIds(Collection<UUID> ids);
}
