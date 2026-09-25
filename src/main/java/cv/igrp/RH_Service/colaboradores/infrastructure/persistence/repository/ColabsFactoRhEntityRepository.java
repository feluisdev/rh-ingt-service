package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FactoRhEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ColabsFactoRhEntityRepository extends JpaRepository<FactoRhEntity, UUID> {

    @Query("""
            SELECT f FROM ColabsFactoRhEntity f
            WHERE f.mesCompetencia = :mes
            ORDER BY f.registadoEm, f.id""")
    List<FactoRhEntity> findByMes(@Param("mes") String mes);

    @Query("""
            SELECT f FROM ColabsFactoRhEntity f
            WHERE f.funcionario.id = :funcionarioId
            ORDER BY f.dataEfeito DESC, f.registadoEm DESC""")
    List<FactoRhEntity> findByFuncionario(@Param("funcionarioId") UUID funcionarioId);
}
