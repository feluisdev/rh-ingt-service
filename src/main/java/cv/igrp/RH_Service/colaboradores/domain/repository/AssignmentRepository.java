package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssignmentRepository {
    Assignment save(Assignment assignment);
    Optional<Assignment> findById(AssignmentId id);
    Optional<Assignment> findCurrentPrincipalByFuncionario(FuncionarioId funcionarioId);
    List<Assignment> findCurrentByFuncionario(FuncionarioId funcionarioId);
    List<Assignment> findAllByFuncionarioOrderByDataInicioDesc(FuncionarioId funcionarioId);
    Optional<Assignment> findCurrentByPosition(UUID positionId);
    boolean isPositionOccupied(UUID positionId);
}
