package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ColocacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsColocacaoEntityRepository extends JpaRepository<ColocacaoEntity, UUID> {

    List<ColocacaoEntity> findByFuncionarioIdAndIsActiveTrue(UUID funcionarioId);

    List<ColocacaoEntity> findByFuncionarioIdAndIsCurrentTrueAndIsActiveTrue(UUID funcionarioId);

    Optional<ColocacaoEntity> findFirstByFuncionarioIdAndIsCurrentTrueAndIsActiveTrue(UUID funcionarioId);

    boolean existsByFuncionarioId(UUID funcionarioId);

    @Modifying
    @Query("UPDATE ColabsColocacaoEntity c SET c.endDate = :endDate, c.isCurrent = false " +
           "WHERE c.funcionarioId = :funcionarioId AND c.isCurrent = true AND c.isActive = true")
    void fecharColocacaoAtual(@Param("funcionarioId") UUID funcionarioId, @Param("endDate") LocalDate endDate);

    Optional<ColocacaoEntity> findFirstByFuncionarioIdAndAssignmentTypeNotAndIsActiveTrueOrderByStartDateDesc(
            UUID funcionarioId, String assignmentType);

    long countByUnitIdAndIsCurrentTrueAndIsActiveTrue(UUID unitId);
}
