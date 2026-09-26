package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AcidenteServicoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsAcidenteServicoEntityRepository extends JpaRepository<AcidenteServicoEntity, UUID> {

    @Query("""
            SELECT a FROM ColabsAcidenteServicoEntity a
            WHERE (:estado IS NULL OR a.estado = :estado)
            ORDER BY a.dataHora DESC""")
    List<AcidenteServicoEntity> find(@Param("estado") String estado);

    @Query("""
            SELECT a FROM ColabsAcidenteServicoEntity a
            WHERE a.funcionario.id = :funcionarioId
            ORDER BY a.dataHora DESC""")
    List<AcidenteServicoEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);
}
