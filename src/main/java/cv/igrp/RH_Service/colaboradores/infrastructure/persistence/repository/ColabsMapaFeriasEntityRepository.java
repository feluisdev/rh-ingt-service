package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.MapaFeriasEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ColabsMapaFeriasEntityRepository extends JpaRepository<MapaFeriasEntity, UUID> {

    Optional<MapaFeriasEntity> findByAno(int ano);
}
