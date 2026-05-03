package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.PedidoAusenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ColabsPedidoAusenciaEntityRepository extends JpaRepository<PedidoAusenciaEntity, UUID> {

    List<PedidoAusenciaEntity> findAllByFuncionarioId(UUID funcionarioId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM ColabsPedidoAusenciaEntity p " +
           "WHERE p.funcionarioId = :funcionarioId " +
           "AND p.estado IN ('APROVADO', 'PENDENTE') " +
           "AND p.dataInicio <= :dataFim " +
           "AND p.dataFim >= :dataInicio")
    boolean existsOverlap(@Param("funcionarioId") UUID funcionarioId,
                          @Param("dataInicio") LocalDate dataInicio,
                          @Param("dataFim") LocalDate dataFim);
}
