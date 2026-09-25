package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ContratoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsContratoEntityRepository extends JpaRepository<ContratoEntity, UUID> {
    Optional<ContratoEntity> findByFuncionario_IdAndIsCurrentTrue(UUID funcionarioId);

    @org.springframework.data.jpa.repository.Query("""
            SELECT c FROM ColabsContratoEntity c
            WHERE c.isCurrent = true AND c.endDate >= :de AND c.endDate <= :ate AND c.funcionario.isActive = true
            ORDER BY c.endDate""")
    List<ContratoEntity> findCorrentesComFimEntre(@org.springframework.data.repository.query.Param("de") java.time.LocalDate de,
                                                  @org.springframework.data.repository.query.Param("ate") java.time.LocalDate ate);
    boolean existsByContractNumber(String contractNumber);
    boolean existsByContractNumberAndIdNot(String contractNumber, UUID id);
    List<ContratoEntity> findByFuncionario_IdOrderByStartDateDesc(UUID funcionarioId);
}
