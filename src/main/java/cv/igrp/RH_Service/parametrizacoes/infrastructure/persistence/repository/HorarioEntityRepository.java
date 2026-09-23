package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.HorarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HorarioEntityRepository extends JpaRepository<HorarioEntity, UUID> {

    List<HorarioEntity> findAllByOrderByNomeAsc();

    List<HorarioEntity> findAllByIsActiveOrderByNomeAsc(Boolean isActive);

    Optional<HorarioEntity> findFirstByIsBaseTrue();
}
