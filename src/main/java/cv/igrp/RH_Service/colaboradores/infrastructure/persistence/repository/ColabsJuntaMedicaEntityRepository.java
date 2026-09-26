package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.JuntaMedicaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsJuntaMedicaEntityRepository extends JpaRepository<JuntaMedicaEntity, UUID> {

    @Query("""
            SELECT j FROM ColabsJuntaMedicaEntity j
            WHERE (:estado IS NULL OR j.estado = :estado)
            ORDER BY j.dataPedido DESC""")
    List<JuntaMedicaEntity> find(@Param("estado") String estado);

    @Query("""
            SELECT j FROM ColabsJuntaMedicaEntity j
            WHERE j.funcionario.id = :funcionarioId
            ORDER BY j.dataPedido DESC""")
    List<JuntaMedicaEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);
}
