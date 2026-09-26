package cv.igrp.RH_Service.formacao.infrastructure.persistence.repository;

import cv.igrp.RH_Service.formacao.infrastructure.persistence.entity.AccaoFormacaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface FormAccaoFormacaoEntityRepository extends JpaRepository<AccaoFormacaoEntity, UUID> {

    @Query("""
            SELECT a FROM FormAccaoFormacaoEntity a
            WHERE (:estado IS NULL OR a.estado = :estado) AND (:ano IS NULL OR year(a.inicio) = :ano)
            ORDER BY a.inicio DESC""")
    List<AccaoFormacaoEntity> find(@Param("estado") String estado, @Param("ano") Integer ano);

    @Query("""
            SELECT DISTINCT a FROM FormAccaoFormacaoEntity a JOIN a.inscricoes i
            WHERE i.funcionario.id = :funcionarioId
            ORDER BY a.inicio DESC""")
    List<AccaoFormacaoEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);

    @Query("""
            SELECT DISTINCT a FROM FormAccaoFormacaoEntity a JOIN a.inscricoes i
            WHERE i.funcionario.id = :funcionarioId AND a.estado IN ('EM_CURSO', 'CONCLUIDA')
              AND i.estado IN ('ADMITIDA', 'APROVEITAMENTO', 'SEM_APROVEITAMENTO')
              AND a.inicio <= :ate AND a.fim >= :de""")
    List<AccaoFormacaoEntity> findComFormandoEntre(@Param("funcionarioId") UUID funcionarioId, @Param("de") LocalDate de,
                                                   @Param("ate") LocalDate ate);

    @Query("""
            SELECT DISTINCT a FROM FormAccaoFormacaoEntity a JOIN a.inscricoes i
            WHERE i.funcionario.id = :funcionarioId AND i.garantiaAte >= :em""")
    List<AccaoFormacaoEntity> findComGarantiaEm(@Param("funcionarioId") UUID funcionarioId, @Param("em") LocalDate em);
}
