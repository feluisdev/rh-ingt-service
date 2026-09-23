package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.HorarioColaboradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsHorarioColaboradorEntityRepository extends JpaRepository<HorarioColaboradorEntity, UUID> {

    @Query("SELECT h FROM ColabsHorarioColaboradorEntity h WHERE h.funcionario.id = :funcionarioId ORDER BY h.dataInicio")
    List<HorarioColaboradorEntity> findByFuncionarioId(@Param("funcionarioId") UUID funcionarioId);

    @Query("""
            SELECT h FROM ColabsHorarioColaboradorEntity h
            WHERE h.funcionario.id = :funcionarioId
              AND h.dataInicio <= :data AND (h.dataFim IS NULL OR h.dataFim >= :data)""")
    Optional<HorarioColaboradorEntity> findVigente(@Param("funcionarioId") UUID funcionarioId,
                                                   @Param("data") LocalDate data);
}
