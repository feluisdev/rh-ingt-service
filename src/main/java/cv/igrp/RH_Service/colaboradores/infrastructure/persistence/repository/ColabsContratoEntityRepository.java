package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ContratoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ColabsContratoEntityRepository extends JpaRepository<ContratoEntity, UUID> {
    boolean existsByFuncionarioIdAndIsActiveTrue(UUID funcionarioId);
    long countByFuncionarioIdAndIsActiveTrue(UUID funcionarioId);
    boolean existsByNumeroContrato(String numeroContrato);
    boolean existsByNumeroContratoAndIdNot(String numeroContrato, UUID id);
    List<ContratoEntity> findByFuncionarioIdOrderByDataInicioDesc(UUID funcionarioId);
}
