package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.VinculoLaboralEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface VinculoLaboralEntityRepository extends JpaRepository<VinculoLaboralEntity, UUID>, JpaSpecificationExecutor<VinculoLaboralEntity> {
    boolean existsByCode(String code);
}
