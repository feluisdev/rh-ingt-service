package cv.igrp.RH_Service.estrutura.domain.repository;

import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PositionRepository {
    Position save(Position position);
    Optional<Position> findById(PositionId id);
    Optional<Position> findByNumeroLugar(String numeroLugar);
    Optional<Position> findResponsavelDeUnidade(UUID unidadeOrganicaId);
    List<Position> findByUnidade(UUID unidadeOrganicaId);
    boolean existsByNumeroLugar(String numeroLugar);
    boolean existsByNumeroLugarAndIdNot(String numeroLugar, PositionId id);
}
