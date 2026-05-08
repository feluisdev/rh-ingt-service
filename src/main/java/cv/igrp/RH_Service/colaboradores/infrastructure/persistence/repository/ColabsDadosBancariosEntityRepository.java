package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DadosBancariosEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ColabsDadosBancariosEntityRepository extends JpaRepository<DadosBancariosEntity, UUID> {
    List<DadosBancariosEntity> findByFuncionarioId(UUID funcionarioId);
}
