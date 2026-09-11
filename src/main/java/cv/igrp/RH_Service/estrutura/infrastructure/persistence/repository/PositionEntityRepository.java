package cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository;

import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.PositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionEntityRepository
        extends JpaRepository<PositionEntity, UUID>, JpaSpecificationExecutor<PositionEntity> {

    boolean existsByNumeroLugar(String numeroLugar);
    boolean existsByNumeroLugarAndIdNot(String numeroLugar, UUID id);
    Optional<PositionEntity> findByNumeroLugar(String numeroLugar);
    Optional<PositionEntity> findByManagesUnit_IdAndIsActiveTrue(UUID managesUnitId);
    List<PositionEntity> findByUnidadeOrganica_IdAndIsActiveTrue(UUID unidadeOrganicaId);
}
