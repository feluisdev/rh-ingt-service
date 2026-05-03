package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.ProfessionalSituationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ProfessionalSituationEntityRepository extends JpaRepository<ProfessionalSituationEntity, UUID>, JpaSpecificationExecutor<ProfessionalSituationEntity> {
    boolean existsByCode(String code);
}
