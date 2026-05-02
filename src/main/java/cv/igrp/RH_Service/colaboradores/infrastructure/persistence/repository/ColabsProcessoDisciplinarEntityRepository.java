package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ProcessoDisciplinarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ColabsProcessoDisciplinarEntityRepository extends JpaRepository<ProcessoDisciplinarEntity, UUID> {
    List<ProcessoDisciplinarEntity> findAllByFuncionarioId(UUID funcionarioId);
}
