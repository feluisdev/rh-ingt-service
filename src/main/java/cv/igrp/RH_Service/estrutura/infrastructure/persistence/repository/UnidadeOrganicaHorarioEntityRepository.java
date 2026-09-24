package cv.igrp.RH_Service.estrutura.infrastructure.persistence.repository;

import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.UnidadeOrganicaHorarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface UnidadeOrganicaHorarioEntityRepository extends JpaRepository<UnidadeOrganicaHorarioEntity, UUID> {
    List<UnidadeOrganicaHorarioEntity> findAllByUnidadeId(UUID unidadeId);
    List<UnidadeOrganicaHorarioEntity> findAllByHorarioId(UUID horarioId);
    List<UnidadeOrganicaHorarioEntity> findAllByUnidadeIdAndDesde(UUID unidadeId, LocalDate desde);
}
