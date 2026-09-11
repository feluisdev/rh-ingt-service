package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.LicencaMobilidadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ColabsLicencaMobilidadeEntityRepository extends JpaRepository<LicencaMobilidadeEntity, UUID> {

    List<LicencaMobilidadeEntity> findAllByFuncionario_Id(UUID funcionarioId);

    List<LicencaMobilidadeEntity> findAllByFuncionario_IdAndIsActiveTrue(UUID funcionarioId);
}
