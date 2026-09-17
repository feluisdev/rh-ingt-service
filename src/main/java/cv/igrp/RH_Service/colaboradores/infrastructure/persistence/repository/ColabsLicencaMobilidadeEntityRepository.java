package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.LicencaMobilidadeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

import java.util.List;
import java.util.UUID;

public interface ColabsLicencaMobilidadeEntityRepository extends JpaRepository<LicencaMobilidadeEntity, UUID> {

    List<LicencaMobilidadeEntity> findAllByFuncionario_Id(UUID funcionarioId);

    List<LicencaMobilidadeEntity> findAllByFuncionario_IdAndIsActiveTrue(UUID funcionarioId);

    @Query("""
            SELECT l FROM ColabsLicencaMobilidadeEntity l
             WHERE l.funcionario.id = :funcionarioId
               AND l.status = 'ACTIVE'
               AND l.dataInicio <= :data
               AND (l.dataFim IS NULL OR l.dataFim >= :data)
             ORDER BY l.dataInicio DESC
            """)
    List<LicencaMobilidadeEntity> findActiveAt(@Param("funcionarioId") UUID funcionarioId,
                                               @Param("data") LocalDate data);
}
