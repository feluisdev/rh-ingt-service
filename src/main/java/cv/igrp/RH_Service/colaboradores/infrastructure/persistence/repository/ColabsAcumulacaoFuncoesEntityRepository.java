package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AcumulacaoFuncoesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ColabsAcumulacaoFuncoesEntityRepository extends JpaRepository<AcumulacaoFuncoesEntity, UUID> {

    @Query("""
            SELECT a FROM ColabsAcumulacaoFuncoesEntity a
            WHERE (:estado IS NULL OR a.estado = :estado)
            ORDER BY a.inicio DESC""")
    List<AcumulacaoFuncoesEntity> find(@Param("estado") String estado);

    @Query("""
            SELECT a FROM ColabsAcumulacaoFuncoesEntity a
            WHERE a.funcionario.id = :funcionarioId
            ORDER BY a.inicio DESC""")
    List<AcumulacaoFuncoesEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);

    @Query("""
            SELECT a FROM ColabsAcumulacaoFuncoesEntity a
            WHERE a.estado = 'AUTORIZADA' AND a.fim IS NOT NULL AND a.fim <= :dia""")
    List<AcumulacaoFuncoesEntity> findAutorizadasComFimAte(@Param("dia") LocalDate dia);
}
