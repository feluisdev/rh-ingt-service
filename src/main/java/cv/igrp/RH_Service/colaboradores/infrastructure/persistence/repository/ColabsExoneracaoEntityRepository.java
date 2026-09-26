package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ExoneracaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsExoneracaoEntityRepository extends JpaRepository<ExoneracaoEntity, UUID> {

    @Query("""
            SELECT e FROM ColabsExoneracaoEntity e
            WHERE (:estado IS NULL OR e.estado = :estado)
            ORDER BY e.dataPreAviso DESC""")
    List<ExoneracaoEntity> find(@Param("estado") String estado);

    @Query("""
            SELECT e FROM ColabsExoneracaoEntity e
            WHERE e.funcionario.id = :funcionarioId
            ORDER BY e.dataPreAviso DESC""")
    List<ExoneracaoEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);
}
