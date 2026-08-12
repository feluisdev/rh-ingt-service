package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ContratoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsContratoEntityRepository extends JpaRepository<ContratoEntity, UUID> {
    Optional<ContratoEntity> findByFuncionario_IdAndIsCurrentTrue(UUID funcionarioId);
    boolean existsByContractNumber(String contractNumber);
    boolean existsByContractNumberAndIdNot(String contractNumber, UUID id);
    List<ContratoEntity> findByFuncionario_IdOrderByStartDateDesc(UUID funcionarioId);
}
