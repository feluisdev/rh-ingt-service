package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.PeriodoProvaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ColabsPeriodoProvaEntityRepository extends JpaRepository<PeriodoProvaEntity, UUID> {

    @Query("""
            SELECT p FROM ColabsPeriodoProvaEntity p
            WHERE p.funcionario.id = :funcionarioId
            ORDER BY p.inicio DESC""")
    List<PeriodoProvaEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);

    @Query("""
            SELECT p FROM ColabsPeriodoProvaEntity p
            WHERE p.estado = 'EM_CURSO' AND p.fimPrevisto >= :de AND p.fimPrevisto <= :ate
            ORDER BY p.fimPrevisto""")
    List<PeriodoProvaEntity> findEmCursoComFimEntre(@Param("de") LocalDate de, @Param("ate") LocalDate ate);

    @Query("""
            SELECT p FROM ColabsPeriodoProvaEntity p
            WHERE p.estado = 'EM_CURSO' AND p.tutor.id = :tutorId
            ORDER BY p.fimPrevisto""")
    List<PeriodoProvaEntity> findEmCursoDoTutor(@Param("tutorId") UUID tutorId);
}
