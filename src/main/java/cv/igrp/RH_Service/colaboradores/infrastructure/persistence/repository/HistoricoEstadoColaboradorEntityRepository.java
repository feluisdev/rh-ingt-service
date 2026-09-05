package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.HistoricoEstadoColaboradorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoEstadoColaboradorEntityRepository extends JpaRepository<HistoricoEstadoColaboradorEntity, UUID> {
    List<HistoricoEstadoColaboradorEntity> findAllByFuncionario_IdOrderByDataEfectividadeDesc(UUID funcionarioId);
}
