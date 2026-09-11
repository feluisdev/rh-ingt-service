package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ReciboVencimentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsReciboVencimentoEntityRepository extends JpaRepository<ReciboVencimentoEntity, UUID> {

    boolean existsByFuncionario_IdAndPeriodMonthAndPeriodYear(UUID funcionarioId, Integer periodMonth, Integer periodYear);

    List<ReciboVencimentoEntity> findAllByFuncionario_Id(UUID funcionarioId);

    @Query("SELECT r FROM ColabsReciboVencimentoEntity r WHERE r.funcionario.id = :funcionarioId AND r.periodYear = :periodYear")
    List<ReciboVencimentoEntity> findAllByFuncionarioIdAndPeriodYear(@Param("funcionarioId") UUID funcionarioId, @Param("periodYear") int periodYear);

    @Query("SELECT r FROM ColabsReciboVencimentoEntity r WHERE r.funcionario.id = :funcionarioId AND r.periodYear = :periodYear AND r.periodMonth = :periodMonth")
    List<ReciboVencimentoEntity> findAllByFuncionarioIdAndPeriodYearAndPeriodMonth(@Param("funcionarioId") UUID funcionarioId, @Param("periodYear") int periodYear, @Param("periodMonth") int periodMonth);
}
