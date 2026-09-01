package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsAssignmentEntityRepository extends JpaRepository<AssignmentEntity, UUID> {

    Optional<AssignmentEntity> findByFuncionarioIdAndIsCurrentTrueAndAssignmentType(UUID funcionarioId, String assignmentType);
    List<AssignmentEntity> findByFuncionarioIdAndIsCurrentTrue(UUID funcionarioId);
    List<AssignmentEntity> findByFuncionarioIdOrderByDataInicioDesc(UUID funcionarioId);
    Optional<AssignmentEntity> findByPositionIdAndIsCurrentTrue(UUID positionId);
    boolean existsByPositionIdAndIsCurrentTrue(UUID positionId);
}
