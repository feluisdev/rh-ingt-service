package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ChecklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ColabsChecklistEntityRepository extends JpaRepository<ChecklistEntity, UUID> {

    @Query("""
            SELECT c FROM ColabsChecklistEntity c
            WHERE c.funcionario.id = :funcionarioId
            ORDER BY c.abertaEm DESC, c.createdDate DESC""")
    List<ChecklistEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);

    @Query("""
            SELECT c FROM ColabsChecklistEntity c
            WHERE c.funcionario.id = :funcionarioId AND c.tipo = :tipo AND c.estado <> 'CANCELADA'
            ORDER BY c.abertaEm DESC, c.createdDate DESC""")
    List<ChecklistEntity> findCorrentes(@Param("funcionarioId") UUID funcionarioId, @Param("tipo") String tipo);

    @Query("""
            SELECT DISTINCT c FROM ColabsChecklistEntity c JOIN c.itens i
            WHERE c.estado = 'ABERTA' AND i.estado = 'PENDENTE'
              AND (:tipo IS NULL OR c.tipo = :tipo)
              AND (:responsavel IS NULL OR i.responsavel = :responsavel)
            ORDER BY c.abertaEm""")
    List<ChecklistEntity> findComPendentes(@Param("tipo") String tipo, @Param("responsavel") String responsavel);

    @Query("""
            SELECT DISTINCT c FROM ColabsChecklistEntity c JOIN c.itens i
            WHERE c.estado = 'ABERTA' AND i.estado = 'PENDENTE' AND i.prazo < :atrasadasEm
              AND (:tipo IS NULL OR c.tipo = :tipo)
              AND (:responsavel IS NULL OR i.responsavel = :responsavel)
            ORDER BY c.abertaEm""")
    List<ChecklistEntity> findComAtrasados(@Param("tipo") String tipo, @Param("responsavel") String responsavel,
                                           @Param("atrasadasEm") LocalDate atrasadasEm);

    @Query("""
            SELECT DISTINCT c FROM ColabsChecklistEntity c JOIN c.itens i
            WHERE c.estado = 'ABERTA' AND i.estado = 'PENDENTE' AND i.prazo = :prazo""")
    List<ChecklistEntity> findComPrazoEm(@Param("prazo") LocalDate prazo);
}
