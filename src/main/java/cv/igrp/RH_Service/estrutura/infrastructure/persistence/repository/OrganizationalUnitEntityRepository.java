package cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository;

import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.OrganizationalUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface OrganizationalUnitEntityRepository
        extends JpaRepository<OrganizationalUnitEntity, UUID>, JpaSpecificationExecutor<OrganizationalUnitEntity> {

    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
    boolean existsByParentUnitIdAndIsActiveTrue(UUID parentUnitId);
    Optional<OrganizationalUnitEntity> findByCode(String code);
}
