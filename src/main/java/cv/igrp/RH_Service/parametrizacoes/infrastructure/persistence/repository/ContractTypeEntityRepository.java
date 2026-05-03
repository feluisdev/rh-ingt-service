package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.ContractTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ContractTypeEntityRepository extends JpaRepository<ContractTypeEntity, UUID>, JpaSpecificationExecutor<ContractTypeEntity> {
    boolean existsByCode(String code);
}
