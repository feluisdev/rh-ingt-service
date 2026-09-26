package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ProcessoDisciplinarEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsProcessoDisciplinarEntityRepository extends JpaRepository<ProcessoDisciplinarEntity, UUID> {
    List<ProcessoDisciplinarEntity> findAllByFuncionario_Id(UUID funcionarioId);

    @Query("""
            SELECT p FROM ColabsProcessoDisciplinarEntity p
            WHERE p.fase IS NOT NULL AND p.fase NOT IN ('CONCLUIDO', 'ARQUIVADO')
            ORDER BY p.startDate""")
    List<ProcessoDisciplinarEntity> findEmCurso();

    @Query("""
            SELECT p FROM ColabsProcessoDisciplinarEntity p
            WHERE p.fase = 'NOTIFICADO' AND p.pena IS NOT NULL AND p.efeitosAplicadosEm IS NULL""")
    List<ProcessoDisciplinarEntity> findComPenaPorExecutar();

    @Query("""
            SELECT COUNT(p) > 0 FROM ColabsProcessoDisciplinarEntity p
            WHERE p.funcionario.id = :funcionarioId
              AND p.fase IN ('INSTAURADO', 'EM_INSTRUCAO', 'ACUSADO', 'RELATORIO')
              AND p.especie NOT IN ('INQUERITO', 'SINDICANCIA', 'AVERIGUACOES')""")
    boolean existeArguidoEmCurso(@Param("funcionarioId") UUID funcionarioId);

    @Query("""
            SELECT DISTINCT p FROM ColabsProcessoDisciplinarEntity p LEFT JOIN p.actos a
            WHERE p.funcionario.id = :funcionarioId
              AND (a.tipo = 'SUSPENSAO_PREVENTIVA' OR (p.pena IN ('SUSPENSAO', 'INACTIVIDADE') AND p.efeitosAplicadosEm IS NOT NULL))""")
    List<ProcessoDisciplinarEntity> findComAfastamento(@Param("funcionarioId") UUID funcionarioId);

    @Query("""
            SELECT COUNT(p) FROM ColabsProcessoDisciplinarEntity p
            WHERE p.fase IS NOT NULL AND year(p.startDate) = :ano""")
    long contarDoAno(@Param("ano") int ano);
}
