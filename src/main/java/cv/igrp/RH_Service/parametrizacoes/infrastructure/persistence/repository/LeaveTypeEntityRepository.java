package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.LeaveTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface LeaveTypeEntityRepository extends JpaRepository<LeaveTypeEntity, UUID>, JpaSpecificationExecutor<LeaveTypeEntity> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
    List<LeaveTypeEntity> findAllByIsActive(Boolean isActive);

    /**
     * Tipos activos de um regime legal (V49). O saldo de ferias nasce sozinho e precisa de saber
     * QUAL das linhas do catalogo sao ferias -- pela classificacao, nunca pelo codigo.
     */
    List<LeaveTypeEntity> findAllByRegimeAndIsActive(String regime, Boolean isActive);
}
