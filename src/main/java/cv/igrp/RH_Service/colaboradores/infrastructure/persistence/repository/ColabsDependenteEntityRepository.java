package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DependenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ColabsDependenteEntityRepository extends JpaRepository<DependenteEntity, UUID> {
    List<DependenteEntity> findByFuncionario_Id(UUID funcionarioId);
}
