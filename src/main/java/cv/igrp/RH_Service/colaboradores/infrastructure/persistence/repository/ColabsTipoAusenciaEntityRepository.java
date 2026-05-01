package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.TipoAusenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ColabsTipoAusenciaEntityRepository extends JpaRepository<TipoAusenciaEntity, UUID> {
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, UUID id);
    List<TipoAusenciaEntity> findAllByIsActive(Boolean isActive);
}
