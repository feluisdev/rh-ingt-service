package cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository;

import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.HorarioBaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HorarioBaseEntityRepository extends JpaRepository<HorarioBaseEntity, UUID> {
    List<HorarioBaseEntity> findAllByDesde(LocalDate desde);
}
