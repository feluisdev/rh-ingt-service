package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.SubtipoLicencaMobilidadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ColabsSubtipoLicencaMobilidadeEntityRepository extends JpaRepository<SubtipoLicencaMobilidadeEntity, UUID> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
    List<SubtipoLicencaMobilidadeEntity> findAllByIsActive(Boolean isActive);
    List<SubtipoLicencaMobilidadeEntity> findAllByRecordType(String recordType);
    List<SubtipoLicencaMobilidadeEntity> findAllByIsActiveAndRecordType(Boolean isActive, String recordType);
}
