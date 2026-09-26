package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FechoMensalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsFechoMensalEntityRepository extends JpaRepository<FechoMensalEntity, UUID> {

    Optional<FechoMensalEntity> findByMes(String mes);

    @Query("SELECT f FROM ColabsFechoMensalEntity f ORDER BY f.mes DESC")
    List<FechoMensalEntity> findTodos();

    @Query("SELECT f.mes FROM ColabsFechoMensalEntity f WHERE f.estado = 'FECHADO' AND f.mes >= :mes")
    List<String> mesesFechadosDesde(@Param("mes") String mes);
}
