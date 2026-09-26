package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ExameSaudeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ColabsExameSaudeEntityRepository extends JpaRepository<ExameSaudeEntity, UUID> {

    @Query("""
            SELECT e FROM ColabsExameSaudeEntity e
            WHERE e.funcionario.id = :funcionarioId
            ORDER BY e.data DESC, e.createdDate DESC""")
    List<ExameSaudeEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);

    /** O último exame de cada colaborador activo, se a validade termina neste dia. */
    @Query("""
            SELECT e FROM ColabsExameSaudeEntity e
            WHERE e.validadeAte = :dia AND e.funcionario.isActive = true
              AND NOT EXISTS (SELECT 1 FROM ColabsExameSaudeEntity o
                              WHERE o.funcionario.id = e.funcionario.id AND o.data > e.data)""")
    List<ExameSaudeEntity> findUltimosComValidadeEm(@Param("dia") LocalDate dia);
}
