package cv.igrp.RH_Service.recrutamento.infrastructure.persistence.repository;

import cv.igrp.RH_Service.recrutamento.infrastructure.persistence.entity.CandidaturaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RecrutCandidaturaEntityRepository extends JpaRepository<CandidaturaEntity, UUID> {

    @Query("""
            SELECT c FROM RecrutCandidaturaEntity c
            WHERE c.concurso.id = :concursoId
            ORDER BY c.dataApresentacao, c.createdDate""")
    List<CandidaturaEntity> findDoConcurso(@Param("concursoId") UUID concursoId);

    @Query("""
            SELECT COUNT(c) > 0 FROM RecrutCandidaturaEntity c
            WHERE c.concurso.id = :concursoId AND c.estado <> 'DESISTIU'
              AND ((:documento IS NOT NULL AND c.documento = :documento) OR (:funcionarioId IS NOT NULL AND c.funcionarioId = :funcionarioId))""")
    boolean existe(@Param("concursoId") UUID concursoId, @Param("documento") String documento,
                   @Param("funcionarioId") UUID funcionarioId);

    @Query("""
            SELECT c FROM RecrutCandidaturaEntity c
            WHERE c.funcionarioId = :funcionarioId
            ORDER BY c.dataApresentacao DESC""")
    List<CandidaturaEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);
}
