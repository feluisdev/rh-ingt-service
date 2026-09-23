package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.ParametroFeriasEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParametroFeriasEntityRepository extends JpaRepository<ParametroFeriasEntity, UUID> {

    Optional<ParametroFeriasEntity> findFirstByVigenteDesdeLessThanEqualOrderByVigenteDesdeDesc(Integer ano);

    List<ParametroFeriasEntity> findAllByOrderByVigenteDesdeAsc();

    boolean existsByVigenteDesde(Integer vigenteDesde);
}
