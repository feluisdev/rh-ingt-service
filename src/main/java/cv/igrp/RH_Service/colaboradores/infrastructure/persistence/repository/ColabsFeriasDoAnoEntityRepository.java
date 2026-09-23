package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FeriasDoAnoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsFeriasDoAnoEntityRepository extends JpaRepository<FeriasDoAnoEntity, UUID> {

    @Query("SELECT a FROM ColabsFeriasDoAnoEntity a WHERE a.funcionario.id = :funcionarioId AND a.ano = :ano")
    Optional<FeriasDoAnoEntity> findByFuncionarioIdAndAno(@Param("funcionarioId") UUID funcionarioId,
                                                          @Param("ano") int ano);

    @Query("SELECT a FROM ColabsFeriasDoAnoEntity a WHERE a.ano = :ano AND a.origem IS NOT NULL")
    List<FeriasDoAnoEntity> findAllComMarcacaoByAno(@Param("ano") int ano);

    @Query("""
            SELECT f.id FROM ColabsFuncionarioEntity f
            WHERE f.isActive = true
              AND NOT EXISTS (SELECT 1 FROM ColabsFeriasDoAnoEntity a
                              WHERE a.funcionario = f AND a.ano = :ano AND a.origem IS NOT NULL)
            ORDER BY f.numeroFuncionario""")
    List<UUID> findFuncionariosActivosSemMarcacao(@Param("ano") int ano);

    @Query("SELECT a.funcionario.id FROM ColabsFeriasDoAnoEntity a WHERE a.ano = :ano AND a.preferenciaIndicadaEm IS NOT NULL")
    List<UUID> findFuncionariosComPreferencia(@Param("ano") int ano);
}
